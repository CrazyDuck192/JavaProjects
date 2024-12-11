// Проект з Java - Пузанов Павло, 3 курс, "Комп'ютерна математика", 2 група
// Тема 5 - "Тензор"
package src;

import org.json.simple.JSONArray;
import org.json.simple.parser.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

/**
 * Клас Tensor представляє багатовимірний масив чисел із підтримкою різних
 * операцій,
 * таких як зчитування, запис, зміна форми, застосування функцій і збереження в
 * JSON/XML.
 *
 * @param <T> Тип чисел, що використовуються в тензорі (наприклад, Integer,
 *            Double).
 */
public class Tensor<T extends Number> {
    /**
     * Масив, що зберігає розмірність тензора по кожній осі.
     */
    private int[] shape;
    /**
     * Загальна кількість елементів у тензорі.
     */
    private int size;
    /**
     * Одновимірний масив, який представляє всі елементи тензора.
     */
    private T[] tensorArray;
    /**
     * Функція для перетворення рядків у значення типу T.
     */
    private Function<String, T> converter;
    /**
     * Клас типу T (наприклад, Integer.class, Double.class).
     */
    private Class<T> dType;

    /**
     * Створення тензора із заданою формою та типом даних.
     *
     * @param shape Масив, що визначає форму тензора.
     * @param dtype Клас типу T.
     */
    @SuppressWarnings("unchecked")
    public Tensor(int[] shape, Class<T> dtype) {
        this.shape = shape;

        int size = 1;
        for (int dim : shape) {
            size *= dim;
        }
        this.size = size;
        this.tensorArray = (T[]) new Number[this.size];
        this.converter = getConverter(dtype);
        this.dType = dtype;
    }

    /**
     * Повертає конвертер рядків у значення типу T.
     *
     * @param dType Клас типу T.
     * @return Функція, що перетворює рядок у значення типу T.
     */
    @SuppressWarnings("unchecked")
    private Function<String, T> getConverter(Class<T> dType) {
        switch (dType.getSimpleName()) {
            case "Integer":
                return value -> (T) Integer.valueOf(value);
            case "Short":
                return value -> (T) Short.valueOf(value);
            case "Long":
                return value -> (T) Long.valueOf(value);
            case "Byte":
                return value -> (T) Byte.valueOf(value);
            case "Float":
                return value -> (T) Float.valueOf(value);
            case "Double":
                return value -> (T) Double.valueOf(value);
            default:
                throw new IllegalArgumentException("Unsupported type: " + dType.getName());
        }
    }

    /**
     * Введення значень тензора з консолі.
     */
    public void input() {
        Scanner scanner = new Scanner(System.in);
        inputInner("Tensor", 0, 0, this.size, scanner);
        scanner.close();
    }

    private void inputInner(String positionHint, int position, int dim, int multiplier, Scanner scanner) {
        if (dim == this.shape.length - 1) {
            for (int j = 0; j < this.shape[dim]; j++) {
                System.out.print(positionHint + "[" + j + "]" + " = ");
                this.tensorArray[position + j] = converter.apply(scanner.nextLine());
            }
        } else {
            multiplier /= this.shape[dim];
            for (int i = 0; i < this.shape[dim]; i++) {
                String newPositionHint = positionHint + "[" + i + "]";
                inputInner(newPositionHint, position + i * multiplier, dim + 1, multiplier, scanner);
            }
        }
    }

    /**
     * Виводить тензор у зручному форматі.
     */
    public void print() {
        printInner(0, 0, this.size);
        System.out.print("\n");
    }

    private void printInner(int position, int dim, int multiplier) {
        System.out.print("[");
        if (dim == this.shape.length - 1) {
            for (int i = 0; i < this.shape[dim] - 1; i++) {
                System.out.print(this.tensorArray[position + i] + " ");
            }
            System.out.print(this.tensorArray[position + this.shape[dim] - 1]);
            System.out.print("]");
        } else {
            multiplier /= this.shape[dim];
            for (int i = 0; i < this.shape[dim]; i++) {
                printInner(position + i * multiplier, dim + 1, multiplier);

                if (i != this.shape[dim] - 1) {
                    for (int j = 1; j < this.shape.length - dim; j++) {
                        System.out.print("\n");
                    }
                    for (int j = 0; j <= dim; j++) {
                        System.out.print(" ");
                    }
                }
            }
            System.out.print("]");
        }
    }

    /**
     * Зчитує тензор з JSON-файлу.
     *
     * @param filename Ім'я JSON-файлу.
     * @param dtype    Тип даних елементів тензора.
     * @return Зчитаний тензор.
     * @throws IOException    Якщо виникла помилка при
     *                        зчитуванні файлу.
     * @throws ParseException
     */
    public static <T extends Number> Tensor<T> readFromJson(String filename, Class<T> dtype)
            throws IOException, ParseException, org.json.simple.parser.ParseException {
        String fileString = Files.readString(Paths.get(filename));
        Object obj = new JSONParser().parse(fileString);
        JSONArray array = (JSONArray) obj;
        ArrayList<Integer> shapeList = new ArrayList<>();
        int dimCounter = 1;
        int[] shape = null;

        while (true) {
            try {
                int dimSize = array.size();
                shapeList.add(dimSize);
                array = (JSONArray) array.getFirst();
                dimCounter += 1;
            } catch (ClassCastException e) {
                shape = new int[dimCounter];
                for (int i = 0; i < dimCounter; i++) {
                    shape[i] = shapeList.get(i);
                }
                break;
            }
        }
        Tensor<T> tensor = new Tensor<T>(shape, dtype);
        readFromJsonInner(0, 0, tensor.size, (JSONArray) obj, tensor);

        return tensor;

    }

    private static <T extends Number> void readFromJsonInner(int position, int dim, int multiplier, JSONArray array,
            Tensor<T> tensor) {
        try {
            array.getFirst();

            multiplier /= tensor.shape[dim];
            for (int i = 0; i < tensor.shape[dim]; i++) {
                readFromJsonInner(position + i * multiplier, dim + 1, multiplier, (JSONArray) array.get(i), tensor);
            }
        } catch (ClassCastException e) {
            for (int j = 0; j < tensor.shape[dim]; j++) {
                tensor.tensorArray[position + j] = tensor.converter.apply(array.get(j).toString());
            }
        }
    }

    /**
     * Записує тензор у JSON-файл.
     *
     * @param filename Ім'я JSON-файлу для запису.
     * @throws IOException    Якщо виникла помилка при записі
     *                        у файл.
     * @throws ParseException
     */
    public void writeToJson(String filename) throws IOException, ParseException {
        String tensorString = writeToJsonInner(0, 0, this.size, "");
        JSONArray tensorJSON = (JSONArray) new JSONParser().parse(tensorString);

        FileWriter file = new FileWriter(filename);
        file.write(tensorJSON.toString());
        file.flush();
        file.close();
    }

    private String writeToJsonInner(int position, int dim, int multiplier, String tensorString) {
        if (dim == this.shape.length - 1) {
            tensorString += "[";
            for (int i = 0; i < this.shape[dim]; i++) {
                tensorString += this.tensorArray[position + i];
                if (i != this.shape[dim] - 1) {
                    tensorString += ",";
                }
            }
            tensorString += "]";
        } else {
            tensorString += "[";
            multiplier /= this.shape[dim];
            for (int i = 0; i < this.shape[dim]; i++) {
                tensorString = writeToJsonInner(position + i * multiplier, dim + 1, multiplier, tensorString);
                if (i != this.shape[dim] - 1) {
                    tensorString += ",";
                }
            }
            tensorString += "]";
        }

        return tensorString;
    }

    /**
     * зчитує тензор з XML-файлу.
     *
     * @param filename Ім'я XML-файлу.
     * @param dtype    Тип даних елементів тензора.
     * @return Зчитаний тензор.
     * @throws IOException                  Якщо виникла помилка при зчитуванні
     *                                      файлу.
     * @throws SAXException                 Якщо виникла помилка при парсингу XML.
     * @throws ParserConfigurationException Якщо виникла помилка при налаштуванні
     *                                      парсера.
     */
    public static <T extends Number> Tensor<T> readFromXml(String filename, Class<T> dtype)
            throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(filename));

        NodeList nodeList = doc.getDocumentElement().getChildNodes();
        ArrayList<Integer> shapeList = new ArrayList<>();
        int dimCounter = 0;
        int[] shape = null;

        while (true) {
            int dimSize = 0;
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    dimSize += 1;
                }
            }
            shapeList.add(dimSize);

            if (nodeList.item(1) == null) {
                shape = new int[dimCounter];
                for (int i = 0; i < dimCounter; i++) {
                    shape[i] = shapeList.get(i);
                }
                break;
            }

            nodeList = nodeList.item(1).getChildNodes();
            dimCounter++;
        }
        Tensor<T> tensor = new Tensor<T>(shape, dtype);
        readFromXmlInner(0, 0, tensor.size, doc.getDocumentElement().getChildNodes(), tensor);

        return tensor;
    }

    private static <T extends Number> void readFromXmlInner(int position, int dim, int multiplier, NodeList nodeList,
            Tensor<T> tensor) {
        if (dim != tensor.shape.length - 1) {
            multiplier /= tensor.shape[dim];
            int subPosition = 0;
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeName().equals("Items")) {
                    readFromXmlInner(position + subPosition * multiplier, dim + 1, multiplier,
                            nodeList.item(i).getChildNodes(), tensor);
                    subPosition++;
                }
            }
        } else {
            int subPosition = 0;
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeName().equals("Item")) {
                    tensor.tensorArray[position + subPosition] = tensor.converter
                            .apply(nodeList.item(i).getTextContent());
                    subPosition++;
                }
            }
        }
    }

    /**
     * Записує тензор у XML-файл.
     *
     * @param filename Ім'я XML-файлу для запису.
     * @throws IOException                       Якщо виникла помилка при записі у
     *                                           файл.
     * @throws ParserConfigurationException      Якщо виникла помилка при
     *                                           налаштуванні
     *                                           парсера.
     * @throws TransformerException              Якщо виникла помилка при
     *                                           трансформації
     *                                           XML.
     * @throws TransformerConfigurationException Якщо виникла помилка при
     *                                           налаштуванні трансформера.
     */
    public void writeToXml(String filename)
            throws IOException, ParserConfigurationException, TransformerConfigurationException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("Tensor");
        writeToXmlInner(0, 0, this.size, root, doc);
        doc.appendChild(root);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        StreamResult result = new StreamResult(filename);
        transformer.transform(source, result);
    }

    private void writeToXmlInner(int position, int dim, int multiplier, Element root, Document doc) {
        if (dim != this.shape.length - 1) {
            multiplier /= this.shape[dim];
            for (int i = 0; i < this.shape[dim]; i++) {
                Element row = doc.createElement("Items");
                row.setAttribute("index", Integer.toString(i));
                writeToXmlInner(position + i * multiplier, dim + 1, multiplier, row, doc);
                root.appendChild(row);
            }
        } else {
            for (int i = 0; i < this.shape[dim]; i++) {
                Element value = doc.createElement("Item");
                value.setAttribute("index", Integer.toString(i));
                value.setTextContent(this.tensorArray[position + i].toString());
                root.appendChild(value);
            }
        }
    }

    /**
     * Заповнює тензор певним значенням.
     *
     * @param x Значення для заповнення.
     */
    public void fill(T x) {
        Arrays.fill(tensorArray, x);
    }

    /**
     * Застосовує унітарну функцію до кожного елемента тензора.
     *
     * @param func Унітарна функція для застосування.
     */
    public void apply(UnaryOperator<T> func) {
        for (int i = 0; i < this.tensorArray.length; i++) {
            this.tensorArray[i] = func.apply(this.tensorArray[i]);
        }
    }

    /**
     * Змінює форму тензора.
     *
     * @param shape Нова форма тензора.
     * @throws Exception Якщо нова форма не відповідає розміру тензора.
     */
    public void reshape(int[] shape) throws Exception {
        int newSize = 1;
        for (int dim : shape) {
            newSize *= dim;
        }
        if (newSize == this.size) {
            this.shape = shape;
        } else {
            throw new Exception("Impossible to reshape!");
        }
    }

    /**
     * Повертає значення з тензора за індексами.
     *
     * @param position Масив індексів.
     * @return Значення за заданими індексами.
     * @throws Exception Якщо індекси є некоректними.
     */
    public T getElement(int[] position) throws Exception {
        if (this.shape.length != position.length) {
            throw new Exception("Incorrect position.");
        }
        for (int i = 0; i < position.length; i++) {
            if (position[i] >= this.shape[i] || position[i] < 0) {
                throw new Exception("Incorrect position.");
            }
        }

        int index = 0;
        for (int i = 0; i < position.length; i++) {
            index += position[i] * (int) Math.pow(this.shape[this.shape.length - 1 - i], this.shape.length - 1 - i);
        }
        return this.tensorArray[index];
    }

    /**
     * Встановлює значення у тензор за індексами.
     *
     * @param position Масив індексів.
     * @param value    Значення для встановлення.
     * @throws Exception Якщо індекси є некоректними.
     */
    public void setElement(int[] position, T value) throws Exception {
        if (position.length != this.shape.length) {
            throw new Exception("Incorrect position.");
        }
        for (int i = 0; i < position.length; i++) {
            if (position[i] >= this.shape[i] || position[i] < 0) {
                throw new Exception("Incorrect position.");
            }
        }

        int index = 0;
        for (int i = 0; i < position.length; i++) {
            index += position[i] * (int) Math.pow(this.shape[this.shape.length - 1 - i], this.shape.length - 1 - i);
        }
        this.tensorArray[index] = value;
    }

    /**
     * Повертає зріз тензора з заданими індексами.
     *
     * @param sliceIndices Масив індексів для зрізу.
     * @return Підтензор.
     */
    public Tensor<T> getSlice(int[][] sliceIndices) {
        ArrayList<T> tensorValues = new ArrayList<T>();
        getSliceInner(0, 0, this.size, sliceIndices, tensorValues);

        int[] shape = new int[sliceIndices.length];
        for (int i = 0; i < sliceIndices.length; i++) {
            shape[i] = sliceIndices[i][1] - sliceIndices[i][0] + 1;
        }

        Tensor<T> slicedTensor = new Tensor<T>(shape, dType);
        tensorValues.toArray(slicedTensor.tensorArray);
        return slicedTensor;
    }

    private void getSliceInner(int dim, int position, int multiplier, int[][] sliceIndices, ArrayList<T> tensorValues) {
        if (dim == this.shape.length - 1) {
            for (int i = sliceIndices[dim][0]; i <= sliceIndices[dim][1]; i++) {
                tensorValues.add(this.tensorArray[position + i]);
            }
        } else {
            multiplier /= this.shape[dim];
            for (int j = sliceIndices[dim][0]; j <= sliceIndices[dim][1]; j++) {
                getSliceInner(dim + 1, position + j * multiplier, multiplier, sliceIndices, tensorValues);
            }
        }
    }

    /**
     * Застосовує бінарну функцію до елементів двох тензорів.
     *
     * @param func   Бінарна функція для застосування.
     * @param tensor Другий тензор.
     * @throws Exception Якщо тензори мають різну форму.
     */
    public void applyWith(BiFunction<T, T, T> func, Tensor<T> tensor) throws Exception {
        for (int i = 0; i < this.shape.length; i++) {
            if (this.shape[i] != tensor.shape[i]) {
                throw new Exception("Tensors must have the same shape");
            }
        }

        for (int i = 0; i < this.size; i++) {
            T value = tensor.tensorArray[i];
            this.tensorArray[i] = func.apply(this.tensorArray[i], value);
        }
    }
}