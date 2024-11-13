import java.sql.*;
import java.util.Scanner;

public class QuizCreatorApp {

    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/quizwizaccdb";
    private static final String USER = "root";
    private static final String PASSWORD = "adminpassword1";
    
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Scanner scanner = new Scanner(System.in);

            // Display existing courses or create a new course
            System.out.println("Do you want to create a new course? (y/n)");
            String createCourseChoice = scanner.nextLine();

            int courseId = -1;

            if (createCourseChoice.equalsIgnoreCase("y")) {
                // Create new course
                System.out.print("Enter the course name: ");
                String courseName = scanner.nextLine();
                System.out.print("Enter the course description: ");
                String courseDescription = scanner.nextLine();

                String insertCourseQuery = "INSERT INTO course (course_name, description) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertCourseQuery, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, courseName);
                    pstmt.setString(2, courseDescription);
                    pstmt.executeUpdate();
                    
                    // Get the generated course_id
                    ResultSet rs = pstmt.getGeneratedKeys();
                    if (rs.next()) {
                        courseId = rs.getInt(1);
                        System.out.println("New course created with ID: " + courseId);
                    }
                }
            } else {
                // List available courses
                System.out.println("Available Courses:");
                String courseQuery = "SELECT * FROM course";
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(courseQuery)) {
                    int courseCount = 1;
                    while (rs.next()) {
                        System.out.println(courseCount++ + ". " + rs.getString("course_name"));
                    }
                }

                // Select a course
                System.out.print("Enter the number of the course you want to choose: ");
                int courseChoice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                // Get selected course ID
                courseId = getSelectedCourseId(conn, courseChoice);
            }

            // Create topic for the selected course
            System.out.print("Enter the topic name for the course: ");
            String topicName = scanner.nextLine();
            System.out.print("Enter the topic description: ");
            String topicDescription = scanner.nextLine();

            String insertTopicQuery = "INSERT INTO topic (course_id, topic_name, description) VALUES (?, ?, ?)";
            int topicId = -1; // Store topic ID here
            try (PreparedStatement pstmt = conn.prepareStatement(insertTopicQuery, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, courseId);
                pstmt.setString(2, topicName);
                pstmt.setString(3, topicDescription);
                pstmt.executeUpdate();
                
                // Get the generated topic_id
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    topicId = rs.getInt(1);
                    System.out.println("New topic created with ID: " + topicId);
                }
            }

            // Add questions to this topic
            boolean addMoreQuestions = true;
            while (addMoreQuestions) {
                System.out.println("\nEnter the question text: ");
                String questionText = scanner.nextLine();

                // Insert question with the correct topic_id
                String insertQuestionQuery = "INSERT INTO question (topic_id, question_text) VALUES (?, ?)";
                int questionId = -1;
                try (PreparedStatement pstmtQuestion = conn.prepareStatement(insertQuestionQuery, Statement.RETURN_GENERATED_KEYS)) {
                    pstmtQuestion.setInt(1, topicId);  // Use the topic_id from the previous step
                    pstmtQuestion.setString(2, questionText);
                    pstmtQuestion.executeUpdate();
                    
                    // Get the generated question_id
                    ResultSet questionRs = pstmtQuestion.getGeneratedKeys();
                    if (questionRs.next()) {
                        questionId = questionRs.getInt(1);
                        System.out.println("New question created with ID: " + questionId);
                    }

                    // Add options to the question
                    for (int i = 1; i <= 4; i++) {
                        System.out.print("Enter option " + i + ": ");
                        String optionText = scanner.nextLine();

                        String insertOptionQuery = "INSERT INTO question_option (question_id, option_text) VALUES (?, ?)";
                        try (PreparedStatement pstmtOption = conn.prepareStatement(insertOptionQuery)) {
                            pstmtOption.setInt(1, questionId);
                            pstmtOption.setString(2, optionText);
                            pstmtOption.executeUpdate();
                        }
                    }

                    // Set the correct answer for this question
                    System.out.print("Enter the number of the correct answer (1-4): ");
                    int correctAnswer = scanner.nextInt();
                    scanner.nextLine(); // consume newline

                    String updateCorrectAnswerQuery = "UPDATE question SET correct_option_id = ? WHERE question_id = ?";
                    try (PreparedStatement pstmtCorrectAnswer = conn.prepareStatement(updateCorrectAnswerQuery)) {
                        pstmtCorrectAnswer.setInt(1, correctAnswer);
                        pstmtCorrectAnswer.setInt(2, questionId);
                        pstmtCorrectAnswer.executeUpdate();
                    }

                    System.out.print("Do you want to add another question? (y/n): ");
                    String addAnotherQuestion = scanner.nextLine();
                    addMoreQuestions = addAnotherQuestion.equalsIgnoreCase("y");
                }
            }

            System.out.println("Quiz creation completed!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int getSelectedCourseId(Connection conn, int courseChoice) throws SQLException {
        String courseQuery = "SELECT * FROM course";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(courseQuery)) {
            int currentCourse = 1;
            while (rs.next()) {
                if (currentCourse == courseChoice) {
                    return rs.getInt("course_id");
                }
                currentCourse++;
            }
        }
        return -1; // Invalid course choice
    }
}
