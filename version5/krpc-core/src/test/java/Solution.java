import java.util.*;

public class Solution {
    public List<List<String>> groupAnagrams(String[] strs) {
        Map<String, List<String>> resultMap = new HashMap<>();

        for (String s : strs) {
            char[] chars = s.toCharArray();
            Arrays.sort(chars);
            String sortedStr = new String(chars); // 排序后的字符串作为 key

            resultMap.computeIfAbsent(sortedStr, k -> new ArrayList<>()).add(s);
        }

        // 直接返回 Map 的 values 作为 List<List<String>>
        return new ArrayList<>(resultMap.values());
    }

    public static void main(String[] args) {
        System.out.println("请输入字符串数组（以空格分隔）：");

        Scanner scanner = new Scanner(System.in);
        String[] inputStrings = scanner.nextLine().split(" ");
        scanner.close(); // 关闭 Scanner，避免资源泄漏

        Solution solution = new Solution();
        List<List<String>> resultList = solution.groupAnagrams(inputStrings);

        // 直接打印
        resultList.forEach(System.out::println);
    }
}
