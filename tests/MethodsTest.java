package tests;

import src.Tensor;

public class MethodsTest {
    public static void main(String[] args) {
        try {
            System.out.println("Зчитування тензору з JSON файла та виведення його у консоль:");
            Tensor<Integer> tensorInteger = Tensor.readFromJson("resources/NZ_test.json", Integer.class);
            tensorInteger.print();

            System.out.println();

            System.out.println("Зчитування тензору з XML файла та виведення його у консоль:");
            Tensor<Double> tensorDouble = Tensor.readFromXml("resources/NZ_test.xml", Double.class);
            tensorDouble.print();

            System.out.println();

            System.out.println("Заповнення тензору деяким значенням: ");
            tensorInteger.fill(1);
            tensorInteger.print();

            System.out.println();

            System.out.println("Зміна розмірів тензору: ");
            tensorInteger.reshape(new int[] { 2, 3 });
            tensorInteger.print();

            System.out.println();

            System.out.println("Застосування унарної функції до всіх елементів тензору (піднесення до квадрату): ");
            tensorDouble.apply(a -> a * a);
            tensorDouble.print();

            System.out.println();

            System.out.println("Отримання елементу з указаної позиції (1, 0, 1): ");
            double valueDouble = tensorDouble.getElement(new int[] { 1, 0, 1 });
            System.out.println(valueDouble);

            System.out.println();

            System.out.println("Зміна елементу на вказаній позиції (0, 2): ");
            tensorInteger.setElement(new int[] { 0, 2 }, 2);
            tensorInteger.print();

            System.out.println();

            System.out.println("Отримання зрізу тензора: ");
            Tensor<Integer> tensorIntegerSliced = tensorInteger.getSlice(new int[][] { { 0, 1 }, { 1, 2 } });
            tensorIntegerSliced.print();

            System.out.println();

            System.out.println("Застосування функції бінарної функції до двох тензорів (додавання): ");
            tensorDouble.applyWith((a, b) -> a + b, tensorDouble);
            tensorDouble.print();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
