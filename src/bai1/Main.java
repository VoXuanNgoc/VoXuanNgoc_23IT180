package bai1;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Main {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Thread2Task> thread2Tasks = new ArrayList<>();
        List<Thread3Task> thread3Tasks = new ArrayList<>();

        try {
            File file = new File("student.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("Student");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element studentElement = (Element) nodeList.item(i);
                String name = studentElement.getElementsByTagName("Name").item(0).getTextContent();
                String dateOfBirth = studentElement.getElementsByTagName("DateOfBirth").item(0).getTextContent();
                
                Thread2Task thread2Task = new Thread2Task(name,dateOfBirth);
                thread2Tasks.add(thread2Task);

                Thread3Task thread3Task = new Thread3Task(name,dateOfBirth);
                thread3Tasks.add(thread3Task);

                executor.execute(thread2Task);
                executor.execute(thread3Task);
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Xử lý kết quả từ Thread 2 và Thread 3
        List<String> results = new ArrayList<>();
        for (int i = 0; i < thread2Tasks.size(); i++) {
        	String name = thread2Tasks.get(i).getName();
            String dateOfBirth = thread2Tasks.get(i).getDateOfBirth();
            int age = thread2Tasks.get(i).getAge();
            int sum = thread3Tasks.get(i).getSum();
            boolean isDigitPrime = thread3Tasks.get(i).isDigitPrime();

          //  String result = "Student(age=" + age + ", sum=" + sum + ", isDigitPrime=" + isDigitPrime + ")";
         String result = name+"(age=" + age + ", sum=" + sum + ", isDigitPrime=" + isDigitPrime + ")";
            results.add(result);
        }

        // Tạo file kq.xml
        createResultXML(results);
    }

    private static void createResultXML(List<String> results) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // Root element
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Results");
            doc.appendChild(rootElement);

            // Adding results
            for (String result : results) {
                Element studentElement = doc.createElement("Student");
                studentElement.setTextContent(result);
                rootElement.appendChild(studentElement);
            }

            // Write the content into kq.xml
            File outputFile = new File("kq.xml");
            FileWriter fileWriter = new FileWriter(outputFile);
            fileWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            fileWriter.write(docToString(doc));
            fileWriter.close();

        } catch (ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    private static String docToString(Document doc) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }
}

class Thread2Task implements Runnable {
	private String name;
    private String dateOfBirth;
    private int age;

    public Thread2Task(String name,String dateOfBirth) {
    	this.name=name;
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public void run() {
    	 try {
             // Chuyển đổi dateOfBirth thành LocalDate
             LocalDate birthDate = LocalDate.parse(dateOfBirth);

             // Lấy ngày hiện tại
             LocalDate currentDate = LocalDate.now();

             // Tính tuổi bằng cách tính khoảng cách giữa hai ngày
             Period period = Period.between(birthDate, currentDate);

             // Lấy tuổi từ khoảng cách
             age = period.getYears();
         } catch (Exception e) {
             // Xử lý lỗi nếu ngày sinh không hợp lệ
             e.printStackTrace();
         }
    }
    public String getName() {
        return name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public int getAge() {
        return age;
    }
}

class Thread3Task implements Runnable {
	private String name;
    private String dateOfBirth;
    private int sum;
    private boolean isDigitPrime;

    public Thread3Task(String name,String dateOfBirth) {
    	this.name=name;
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public void run() {
    	try {
            // Tính tổng các chữ số
            int digitSum = calculateDigitSum(dateOfBirth);

            // Lưu tổng vào sum
            sum = digitSum;

            // Kiểm tra xem tổng có phải là số nguyên tố không
            isDigitPrime = isPrime(digitSum);
        } catch (Exception e) {
            // Xử lý lỗi nếu có
            e.printStackTrace();
        }
    }

    public int getSum() {
        return sum;
    }

    public boolean isDigitPrime() {
        return isDigitPrime;
    }

    // Phương thức tính tổng các chữ số trong chuỗi
    private int calculateDigitSum(String str) {
        int sum = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isDigit(c)) {
                sum += Character.getNumericValue(c);
            }
        }
        return sum;
    }

    // Phương thức kiểm tra xem một số có phải là số nguyên tố hay không
    private boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        }
        if (n <= 3) {
            return true;
        }
        if (n % 2 == 0 || n % 3 == 0) {
            return false;
        }
        for (int i = 5; i * i <= n; i = i + 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }
}
