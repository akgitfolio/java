import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVDataAnalyzer {

    // Custom class to represent each row of the CSV file
    static class Employee {
        String firstName;
        String lastName;
        String designation;
        long contact;
        double salary;
        String city;

        public Employee(String[] data) {
            this.firstName = data[0];
            this.lastName = data[1];
            this.designation = data[2];
            this.contact = Long.parseLong(data[3]);
            this.salary = Double.parseDouble(data[4]);
            this.city = data[5];
        }

        @Override
        public String toString() {
            return "Employee [First Name=" + firstName + ", Last Name=" + lastName +
                    ", Designation=" + designation + ", Contact=" + contact +
                    ", Salary=" + salary + ", City=" + city + "]";
        }
    }

    public static void main(String[] args) {
        String csvFile = "employees.csv";
        List<Employee> employees = readCSV(csvFile);

        // Print all employees
        System.out.println("All Employees:");
        for (Employee emp : employees) {
            System.out.println(emp);
        }

        // Perform some analysis
        double totalSalary = 0;
        int managerCount = 0;

        for (Employee emp : employees) {
            totalSalary += emp.salary;
            if (emp.designation.toLowerCase().contains("manager")) {
                managerCount++;
            }
        }

        double averageSalary = totalSalary / employees.size();

        System.out.println("\nAnalysis Results:");
        System.out.println("Total number of employees: " + employees.size());
        System.out.println("Average salary: $" + String.format("%.2f", averageSalary));
        System.out.println("Number of managers: " + managerCount);
    }

    public static List<Employee> readCSV(String fileName) {
        List<Employee> employees = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            // Skip the header line
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 6) {
                    employees.add(new Employee(data));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return employees;
    }
}