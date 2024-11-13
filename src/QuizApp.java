import java.sql.*;
import java.util.Scanner;

public class QuizApp {

    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/quizwizaccdb";
    private static final String USER = "root";
    private static final String PASSWORD = "adminpassword1";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Scanner scanner = new Scanner(System.in);

            // Display all courses
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
            scanner.nextLine(); // consume newline left-over

            // Get selected course ID
            int selectedCourseId = getSelectedCourseId(conn, courseChoice);

            // Display all topics for the selected course
            System.out.println("Available Topics for this course:");
            String topicQuery = "SELECT * FROM topic WHERE course_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(topicQuery)) {
                pstmt.setInt(1, selectedCourseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    int topicCount = 1;
                    while (rs.next()) {
                        System.out.println(topicCount++ + ". " + rs.getString("topic_name"));
                    }
                }
            }

            // Select a topic
            System.out.print("Enter the number of the topic you want to choose: ");
            int topicChoice = scanner.nextInt();
            scanner.nextLine(); // consume newline left-over

            // Get selected topic ID
            int selectedTopicId = getSelectedTopicId(conn, topicChoice, selectedCourseId);

            // Display questions for the selected topic and allow answering
            System.out.println("Answer the following questions:");
            String questionQuery = "SELECT * FROM question WHERE topic_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(questionQuery)) {
                pstmt.setInt(1, selectedTopicId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int questionId = rs.getInt("question_id");
                        String questionText = rs.getString("question_text");

                        // Display question
                        System.out.println(questionText);

                        // Display options for the question
                        displayQuestionOptions(conn, questionId);

                        // Allow user to select an answer
                        System.out.print("Enter the number of your answer: ");
                        int answerChoice = scanner.nextInt();
                        scanner.nextLine(); // consume newline left-over

                        // Check if the answer is correct
                        checkAnswer(conn, questionId, answerChoice);
                    }
                }
            }

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

    private static int getSelectedTopicId(Connection conn, int topicChoice, int courseId) throws SQLException {
        String topicQuery = "SELECT * FROM topic WHERE course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(topicQuery)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                int currentTopic = 1;
                while (rs.next()) {
                    if (currentTopic == topicChoice) {
                        return rs.getInt("topic_id");
                    }
                    currentTopic++;
                }
            }
        }
        return -1; // Invalid topic choice
    }

    private static void displayQuestionOptions(Connection conn, int questionId) throws SQLException {
        String optionQuery = "SELECT * FROM question_option WHERE question_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(optionQuery)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                int optionNumber = 1;
                while (rs.next()) {
                    System.out.println(optionNumber++ + ". " + rs.getString("option_text"));
                }
            }
        }
    }

    private static void checkAnswer(Connection conn, int questionId, int answerChoice) throws SQLException {
        String correctAnswerQuery = "SELECT correct_option_id FROM question WHERE question_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(correctAnswerQuery)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int correctOptionId = rs.getInt("correct_option_id");
                    if (correctOptionId == answerChoice) {
                        System.out.println("Correct answer!");
                    } else {
                        System.out.println("Incorrect answer.");
                    }
                }
            }
        }
    }
}
