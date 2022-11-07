import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    //???


    public BooleanSearchEngine(File pdfsDir) throws IOException {
        if (pdfsDir.isDirectory()) {
            for (File item : pdfsDir.listFiles()) {
                File pdfFile = item.getAbsoluteFile();
                System.out.println(item.getName());  //вывод списка pdf в папке
                booleanSearchEngineOneFile(pdfFile);
            }
        } else {
            booleanSearchEngineOneFile(pdfsDir);
        }
    }

    public void booleanSearchEngineOneFile(File pdfsDir) throws IOException {
        var doc = new PdfDocument(new PdfReader(pdfsDir));
        Map<String, List> docMap = new HashMap<>();   //Мапа файла. key - слово, value - лист с номером страницы и количеством повторов
        for (int i = 0; i < doc.getNumberOfPages(); i++) {

            var page = doc.getPage(i + 1);
            var text = PdfTextExtractor.getTextFromPage(page);
            var words = text.toLowerCase().split("\\P{IsAlphabetic}+");
//            System.out.println(words[0]);
            List<Integer> wordList = new ArrayList<>(); //Лист страницы и повторов. № ячейки - страница, value - количество повторов
            for (int j = 0; j < words.length; j++) {
                if (wordList.isEmpty()) {
                    wordList.add(j, 1);
                } else {
                    wordList.set(j, wordList.get(j) + 1);
                }
//                if (docMap.get(words[j]) == null) {
//                    docMap.put(words[j], wordList);
//                } else {
//                    docMap.replace(words[j], wordList);
//                }

            }


        }
//        System.out.println(docMap);
    }
    // прочтите тут все pdf и сохраните нужные данные,
    // тк во время поиска сервер не должен уже читать файлы


    @Override
    public List<PageEntry> search(String word) {
        // тут реализуйте поиск по слову
        return Collections.emptyList();
    }
}
