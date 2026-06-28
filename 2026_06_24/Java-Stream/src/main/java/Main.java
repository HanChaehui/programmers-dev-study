import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    static void main() {
        List<Product> products = new ArrayList<>(Arrays.asList(
                new Product("연필", 500), new Product("공책", 1200), new Product("지우개", 300),
                new Product("필통", 3000), new Product("볼펜", 800)));

        System.out.println("===== 1. 스트림 만들고 전체 출력 (forEach) =====");
        products.stream().forEach(p -> System.out.println(p.getName() + " (" + p.getPrice() + "원)"));
        System.out.println();

        System.out.println("===== 2. filter: 1000원 이상만 =====");
        products.stream().filter(p -> p.getPrice() >= 1000)
                .forEach((p -> System.out.println(p.getName() + " (" + p.getPrice() + "원)")));
        System.out.println();

        System.out.println("===== 3. map: 이름만 뽑기 =====");
        products.stream().map(p -> p.getName())
                .forEach(name -> System.out.println(name));
        System.out.println();

        List<Order> orders = new ArrayList<>(Arrays.asList(
                new Order(1, Arrays.asList("연필", "공책")),
                new Order(2, Arrays.asList("필통", "볼펜", "공책"))
        ));

        System.out.println("===== 4. map vs flatMap (주문 속 상품 목록) =====");
        List<List<String>> byMap = orders.stream().map(o -> o.getItems()).collect(Collectors.toList());
        System.out.println("map     : " + byMap);
        List<String> byFlatMap = orders.stream().flatMap(o -> o.getItems().stream()).collect(Collectors.toList());
        System.out.println("flatMap : " + byFlatMap);
        System.out.println();

        System.out.println("===== 5. filter + map + collect: 1000원 이상 상품 이름 리스트 =====");
        List<String> names = products.stream()
                .filter(p -> p.getPrice() >= 1000)
                .map(p -> p.getName())
                .collect(Collectors.toList());
        System.out.println(names);
        System.out.println();

        long count = products.stream()
                .filter(p -> p.getPrice() >= 1000)
                .count();

        int sum = products.stream()
                .mapToInt(p -> p.getPrice())
                .sum();

        double avg = products.stream()
                .mapToInt(p -> p.getPrice())
                .average().getAsDouble();

        List<String> byPrice = products.stream()
                .sorted((a, b) -> a.getPrice() - b.getPrice())
                .map(p -> p.getName())
                .collect(Collectors.toList());

        System.out.println("===== 6. 통계 ====");
        System.out.println("1000원 이상 개수: " + count + "\n전체 가격 합계: " + sum + "\n전체 가격 평균: " + avg + "\n가격 오름차순: " + byPrice);
        System.out.println();

    }
}
