import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Main {
    public static void main(String[] args) {
        try {
            // Load the JSON test case from files
            JSONParser parser = new JSONParser();
            JSONObject testCase1 = (JSONObject) parser.parse(new FileReader("testcase1.json"));
            JSONObject testCase2 = (JSONObject) parser.parse(new FileReader("testcase2.json"));

            // Process both test cases
            double secret1 = processTestCase(testCase1);
            System.out.println("Output for Test Case 1: " + secret1);

            double secret2 = processTestCase(testCase2);
            System.out.println("Output for Test Case 2: " + secret2);

            // Detect and print wrong points for test case 2
            detectWrongPoints(testCase2, secret2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Process a single test case, decode points, and calculate the secret using Lagrange interpolation
    public static double processTestCase(JSONObject testCase) {
        // Extract the number of points and required roots
        int n = ((Long) ((JSONObject) testCase.get("keys")).get("n")).intValue();
        int k = ((Long) ((JSONObject) testCase.get("keys")).get("k")).intValue();

        // Create a map to store the decoded points (x, y)
        Map<Integer, Long> points = new HashMap<>();
        for (Object key : testCase.keySet()) {
            if (key.equals("keys")) continue; // Skip the "keys" object
            int x = Integer.parseInt((String) key);
            JSONObject point = (JSONObject) testCase.get(key);
            int base = Integer.parseInt((String) point.get("base"));
            String value = (String) point.get("value");
            long decodedY = Long.parseLong(value, base); // Decode the y-value from the given base
            points.put(x, decodedY);
        }

        // Apply Lagrange interpolation to calculate the constant term (c)
        return lagrangeInterpolation(points, k);
    }

    // Implement Lagrange Interpolation to calculate the constant term
    public static double lagrangeInterpolation(Map<Integer, Long> points, int k) {
        double constantTerm = 0;

        // Lagrange interpolation formula:
        // L(x) = sum of (y_i * product((x - x_j) / (x_i - x_j)) for all j != i)
        for (Map.Entry<Integer, Long> entry : points.entrySet()) {
            int xi = entry.getKey();
            long yi = entry.getValue();

            double li = 1.0;
            for (Map.Entry<Integer, Long> innerEntry : points.entrySet()) {
                int xj = innerEntry.getKey();
                if (xi != xj) {
                    li *= (0.0 - xj) / (double)(xi - xj); // Since we are solving for x = 0 (constant term)
                }
            }
            constantTerm += li * yi;
        }

        return constantTerm;
    }

    // Detect wrong points in the second test case
    public static void detectWrongPoints(JSONObject testCase, double constantTerm) {
        // Re-process the points to check if they satisfy the calculated polynomial
        Map<Integer, Long> points = new HashMap<>();
        for (Object key : testCase.keySet()) {
            if (key.equals("keys")) continue;
            int x = Integer.parseInt((String) key);
            JSONObject point = (JSONObject) testCase.get(key);
            int base = Integer.parseInt((String) point.get("base"));
            String value = (String) point.get("value");
            long decodedY = Long.parseLong(value, base);
            points.put(x, decodedY);
        }

        // Evaluate each point to see if it satisfies the polynomial
        for (Map.Entry<Integer, Long> entry : points.entrySet()) {
            int x = entry.getKey();
            long y = entry.getValue();

            // In this simple case, we are assuming a constant polynomial y = c
            double calculatedY = constantTerm;

            // Check if the point is wrong (tolerance for floating-point comparison)
            if (Math.abs(calculatedY - y) > 1e-6) {
                System.out.println("Wrong point detected: (" + x + ", " + y + ")");
            }
        }
    }
}
