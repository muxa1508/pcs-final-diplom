import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    protected Map<String, List<PageEntry>> fileMap = new HashMap<>();
    protected String stopListFile = "stop-ru.txt";
    protected List<String> stopList = new ArrayList<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        if (pdfsDir.isDirectory()) {
            for (File item : pdfsDir.listFiles()) {
                File pdfFile = item.getAbsoluteFile();
                booleanSearchEngineOneFile(pdfFile);
            }
        } else {
            booleanSearchEngineOneFile(pdfsDir);
        }
        stopListParser(new File(stopListFile));                                                                         //Заполнение коллекции стоп-слов
    }

    protected void booleanSearchEngineOneFile(File pdfsDir) throws IOException {
        var doc = new PdfDocument(new PdfReader(pdfsDir));
        for (int i = 0; i < doc.getNumberOfPages(); i++) {
            int pageInt = i + 1;
            var page = doc.getPage(pageInt);
            var text = PdfTextExtractor.getTextFromPage(page);
            var words = text.toLowerCase().split("\\P{IsAlphabetic}+");
            Map<String, Integer> freqs = new HashMap<>();                                                               //Мапа с частотой повтора слова. key - слово, value - частота повтора.
            for (var word : words) {
                if (word.isEmpty()) {
                    continue;
                }
                freqs.put(word, freqs.getOrDefault(word, 0) + 1);
            }

            freqs.forEach((k, v) -> {
                PageEntry pageEntry = new PageEntry(pdfsDir.getName(), pageInt, v);
                if (!fileMap.containsKey(k)) {
                    fileMap.put(k, new ArrayList<>(List.of(pageEntry)));
                } else {
                    fileMap.get(k).add(pageEntry);
                }
            });
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        List<String> searchList = List.of(word.toLowerCase().split("\\P{IsAlphabetic}+"));
        List<PageEntry> out = new ArrayList<>();
        Map<Map<String, Integer>, Integer> pageMap = new HashMap<>();
        for (String searchWord : searchList) {                                                                          //Перебор по списку поисковых слов
            if (stopList.contains(searchWord)) {                                                                        //Проверка наличия искомого слова в стоп-листе
                break;
            }
            List<PageEntry> searchRequest = fileMap.get(searchWord);

            if (searchList.size() == 1) {
                Collections.sort(searchRequest, PageEntry::compareTo);
                return searchRequest;
            }

            if (searchRequest != null) {
                for (PageEntry pageEntry : searchRequest) {
                    Map<String, Integer> hashName = new HashMap<>();
                    hashName.put(pageEntry.getPdfName(), pageEntry.getPage());
                    if (!pageMap.containsKey(hashName)) {
                        pageMap.put(hashName, pageEntry.getCount());
                    } else {
                        pageMap.replace(hashName, pageMap.get(hashName), pageMap.get(hashName) + pageEntry.getCount());
                    }
                }
            }
        }

        pageMap.forEach((k, v) -> {
            Map<String, Integer> hashName = new HashMap<>(k);
            int count = v;
            hashName.forEach((key, value) -> {
                String name = key;
                int page = value;
                PageEntry newPageEntry = new PageEntry(name, page, count);
                out.add(newPageEntry);
            });
        });
        Collections.sort(out, PageEntry::compareTo);
        return out;
    }

    protected List<String> stopListParser(File inputFile) {
        try (FileReader fileReader = new FileReader(inputFile);
             Scanner scanner = new Scanner(fileReader)) {
            stopList.add(scanner.nextLine());
            return stopList;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONArray outToJson(List out) {
        JSONArray outJson = new JSONArray();
        for (int i = 0; i < out.size(); i++) {
            PageEntry pageEntry = (PageEntry) out.get(i);
            JSONObject pageEntryJson = new JSONObject();
            pageEntryJson.put("pdfName", pageEntry.getPdfName());
            pageEntryJson.put("page", pageEntry.getPage());
            pageEntryJson.put("count", pageEntry.getCount());
            outJson.add(pageEntryJson);
        }
        return outJson;
    }
}
