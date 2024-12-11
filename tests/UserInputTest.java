package tests;

import java.util.Arrays;
import java.util.Scanner;

import src.Tensor;

public class UserInputTest {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println(
                    "Введіть: \n 1 - ввести тензор з консолі,\n 2 - зчитати тензор з JSON файлу NZ_test2.json, \n 3 - зчитати тензор з XML файлу NZ_test2.xml");

            int inputType = scanner.nextInt();

            if (inputType == 1) {
                System.out.println("Оберіть тип тензор: \n 1 - Integer, \n 2 - Double");
                int dataType = scanner.nextInt();
                System.out.print("Введіть розміри тензору (через крапку з комою): ");
                int[] tensorShape = Arrays.stream(scanner.next().split(";")).mapToInt(Integer::parseInt)
                        .toArray();
                if (dataType == 1) {
                    Tensor<Integer> tensorInteger = new Tensor<>(tensorShape, Integer.class);
                    tensorInteger.input();
                    tensorInteger.print();
                    tensorInteger.writeToJson("resources/NZ_test2.json");
                    System.out.println("Тензор успішно записан до файлу NZ_test2.json!");
                    tensorInteger.writeToXml("resources/NZ_test2.xml");
                    System.out.println("Тензор успішно записан до файлу NZ_test2.xml!");
                } else if (dataType == 2) {
                    Tensor<Double> tensorDouble = new Tensor<>(tensorShape, Double.class);
                    tensorDouble.input();
                    tensorDouble.print();
                    tensorDouble.writeToJson("resources/NZ_test2.json");
                    System.out.println("Тензор успішно записан до файлу NZ_test2.json!");
                    tensorDouble.writeToXml("resources/NZ_test2.xml");
                    System.out.println("Тензор успішно записан до файлу NZ_test2.xml!");
                }
            } else if (inputType == 2) {
                Tensor<Integer> tensorInteger = Tensor.readFromJson("resources/NZ_test.json", Integer.class);
                tensorInteger.print();
            } else if (inputType == 3) {
                Tensor<Integer> tensorInteger = Tensor.readFromXml("resources/NZ_test.xml", Integer.class);
                tensorInteger.print();
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
