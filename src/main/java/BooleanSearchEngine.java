import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    protected List<List> fileMapList = new ArrayList<>();
    protected String stopListFile = "stop-ru.txt";


    public BooleanSearchEngine(File pdfsDir) throws IOException {
        if (pdfsDir.isDirectory()) {
            for (File item : pdfsDir.listFiles()) {
                File pdfFile = item.getAbsoluteFile();
                booleanSearchEngineOneFile(pdfFile);
            }
        } else {
            booleanSearchEngineOneFile(pdfsDir);
        }
    }

    protected void booleanSearchEngineOneFile(File pdfsDir) throws IOException {

        List<Map> pageList = new ArrayList<>();
        var doc = new PdfDocument(new PdfReader(pdfsDir));
        for (int i = 0; i < doc.getNumberOfPages(); i++) {
            Map<String, PageEntry> docMap = new HashMap<>();                                                            //Мапа файла. key - слово, value - PageEntry с информацией о имени файла, странице и числе повторов.
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
                docMap.put(k, new PageEntry(pdfsDir.getName(), pageInt, v));                                            //Перебор мапы частоты для присвоения значений в мапу файла.
            });
            pageList.add(docMap);
        }
        fileMapList.add(pageList);
    }

    @Override
    public List<PageEntry> search(String word) {
        List<String> searchList = List.of(word.toLowerCase().split("\\P{IsAlphabetic}+"));
        List<PageEntry> out = new ArrayList<>();
        for (int i = 0; i < fileMapList.size(); i++) {
            List pageList = fileMapList.get(i);
            for (int j = 0; j < pageList.size(); j++) {
                Map<String, PageEntry> docMap = (Map<String, PageEntry>) pageList.get(j);
                PageEntry searchRequestSum = null;                                                                      //Определение общего PageEntry для всех искомых на странице слов
                for (String searchWord : searchList) {                                                                  //Перебор по списку поисковых слов
                    for (String stopListWord : stopListParser(new File(stopListFile))) {                                //перебор по списку стоп-листа
                        if (!searchWord.equals(stopListWord)) {                                                         //Проверка наличия искомого слова в стоп-листе
                            PageEntry searchRequest = docMap.get(searchWord);
                            if (searchRequest != null) {
                                if (searchRequestSum == null) {
                                    searchRequestSum = searchRequest;
                                } else {
                                    searchRequestSum = new PageEntry(searchRequestSum.getPdfName(), searchRequestSum.getPage(), searchRequestSum.getCount() + searchRequest.getCount());
                                }
                            }
                        }

                    }
                }
                if (searchRequestSum != null) {                                                                         //Добавление обобщенного для страницы и всех поисковых слов PageEntry в выходной List
                    out.add(searchRequestSum);
                }
            }
        }
        Collections.sort(out, PageEntry::compareTo);
        return out;
    }

    protected List<String> stopListParser(File inputFile) {
        List<String> stopList = new ArrayList<>();
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
