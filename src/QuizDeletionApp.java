import java.sql.*;
import java.util.Scanner;

public class QuizDeletionApp {

    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/quizwizaccdb";
    private static final String USER = "root";
    private static final String PASSWORD = "adminpassword1";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Scanner scanner = new Scanner(System.in);

            // Display options for deletion
            System.out.println("What do you want to delete?");
            System.out.println("1. Delete a course");
            System.out.println("2. Delete a topic");
            System.out.println("3. Delete a question");
            System.out.print("Enter your choice (1-3): ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume the newline

            switch (choice) {
                case 1:
                    deleteCourse(conn, scanner);
                    break;
                case 2:
                    deleteTopic(conn, scanner);
                    break;
                case 3:
                    deleteQuestion(conn, scanner);
                    break;
                default:
                    System.out.println("Invalid choice.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to delete a course and its related topics, questions, and options
    private static void deleteCourse(Connection conn, Scanner scanner) throws SQLException {
        // Display all courses
        System.out.println("Available Courses:");
        String courseQuery = "SELECT * FROM course";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(courseQuery)) {
            int courseCount = 1;
            while (rs.next()) {
                System.out.println(courseCount++ + ". " + rs.getString("course_name"));
            }
        }

        // Ask user to select a course
        System.out.print("Enter the number of the course you want to delete: ");
        int courseChoice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        int courseId = getCourseIdByChoice(conn, courseChoice);
        if (courseId == -1) {
            System.out.println("Course not found.");
            return;
        }

        // Delete associated question options for all questions in this course's topics
        String deleteOptionsQuery = "DELETE FROM question_option WHERE question_id IN (SELECT question_id FROM question WHERE topic_id IN (SELECT topic_id FROM topic WHERE course_id = ?))";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteOptionsQuery)) {
            pstmt.setInt(1, courseId);
            pstmt.executeUpdate();
        }

        // Delete all questions
        String deleteQuestionsQuery = "DELETE FROM question WHERE topic_id IN (SELECT topic_id FROM topic WHERE course_id = ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteQuestionsQuery)) {
            pstmt.setInt(1, courseId);
            pstmt.executeUpdate();
        }

        // Delete all topics
        String deleteTopicsQuery = "DELETE FROM topic WHERE course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteTopicsQuery)) {
            pstmt.setInt(1, courseId);
            pstmt.executeUpdate();
        }

        // Finally, delete the course
        String deleteCourseQuery = "DELETE FROM course WHERE course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteCourseQuery)) {
            pstmt.setInt(1, courseId);
            pstmt.executeUpdate();
            System.out.println("Course has been deleted.");
        }
    }

    // Method to delete a topic and its related questions and options
    private static void deleteTopic(Connection conn, Scanner scanner) throws SQLException {
        // Ask user to select a course first
        System.out.println("Available Courses:");
        String courseQuery = "SELECT * FROM course";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(courseQuery)) {
            int courseCount = 1;
            while (rs.next()) {
                System.out.println(courseCount++ + ". " + rs.getString("course_name"));
            }
        }

        // Ask user to select a course
        System.out.print("Enter the number of the course for which you want to delete a topic: ");
        int courseChoice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        int courseId = getCourseIdByChoice(conn, courseChoice);
        if (courseId == -1) {
            System.out.println("Course not found.");
            return;
        }

        // Display all topics for the selected course
        System.out.println("Available Topics for this course:");
        String topicQuery = "SELECT topic_name, topic_id FROM topic WHERE course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(topicQuery)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                int topicCount = 1;
                while (rs.next()) {
                    System.out.println(topicCount++ + ". " + rs.getString("topic_name"));
                }
            }
        }

        // Ask user to select a topic
        System.out.print("Enter the number of the topic you want to delete: ");
        int topicChoice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        int topicId = getTopicIdByChoice(conn, topicChoice, courseId);
        if (topicId == -1) {
            System.out.println("Topic not found.");
            return;
        }

        // Delete associated question options
        String deleteOptionsQuery = "DELETE FROM question_option WHERE question_id IN (SELECT question_id FROM question WHERE topic_id = ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteOptionsQuery)) {
            pstmt.setInt(1, topicId);
            pstmt.executeUpdate();
        }

        // Delete the questions related to the topic
        String deleteQuestionsQuery = "DELETE FROM question WHERE topic_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteQuestionsQuery)) {
            pstmt.setInt(1, topicId);
            pstmt.executeUpdate();
        }

        // Finally, delete the topic
        String deleteTopicQuery = "DELETE FROM topic WHERE topic_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteTopicQuery)) {
            pstmt.setInt(1, topicId);
            pstmt.executeUpdate();
            System.out.println("Topic has been deleted.");
        }
    }

    // Method to delete a question and its associated options
    private static void deleteQuestion(Connection conn, Scanner scanner) throws SQLException {
        // Ask user to select a course first
        System.out.println("Available Courses:");
        String courseQuery = "SELECT * FROM course";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(courseQuery)) {
            int courseCount = 1;
            while (rs.next()) {
                System.out.println(courseCount++ + ". " + rs.getString("course_name"));
            }
        }

        // Ask user to select a course
        System.out.print("Enter the number of the course for which you want to delete a question: ");
        int courseChoice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        int courseId = getCourseIdByChoice(conn, courseChoice);
        if (courseId == -1) {
            System.out.println("Course not found.");
            return;
        }

        // Ask user to select a topic within the course
        System.out.println("Available Topics for this course:");
        String topicQuery = "SELECT topic_name, topic_id FROM topic WHERE course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(topicQuery)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                int topicCount = 1;
                while (rs.next()) {
                    System.out.println(topicCount++ + ". " + rs.getString("topic_name"));
                }
            }
        }

        // Ask user to select a topic
        System.out.print("Enter the number of the topic for which you want to delete a question: ");
        int topicChoice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        int topicId = getTopicIdByChoice(conn, topicChoice, courseId);
        if (topicId == -1) {
            System.out.println("Topic not found.");
            return;
        }

        // Display all questions for the selected topic
        System.out.println("Available Questions for this topic:");
        String questionQuery = "SELECT question_text, question_id FROM question WHERE topic_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(questionQuery)) {
            pstmt.setInt(1, topicId);
            try (ResultSet rs = pstmt.executeQuery()) {
                int questionCount = 1;
                while (rs.next()) {
                    System.out.println(questionCount++ + ". " + rs.getString("question_text"));
                }
            }
        }

        // Ask user to select a question
        System.out.print("Enter the number of the question you want to delete: ");
        int questionChoice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        int questionId = getQuestionIdByChoice(conn, questionChoice, topicId);
        if (questionId == -1) {
            System.out.println("Question not found.");
            return;
        }

        // Delete the associated question options
        String deleteOptionsQuery = "DELETE FROM question_option WHERE question_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteOptionsQuery)) {
            pstmt.setInt(1, questionId);
            pstmt.executeUpdate();
        }

        // Finally, delete the question
        String deleteQuestionQuery = "DELETE FROM question WHERE question_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteQuestionQuery)) {
            pstmt.setInt(1, questionId);
            pstmt.executeUpdate();
            System.out.println("Question has been deleted.");
        }
    }

    // Method to get course ID by choice
    private static int getCourseIdByChoice(Connection conn, int courseChoice) throws SQLException {
        String query = "SELECT course_id FROM course";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            int currentCourse = 1;
            while (rs.next()) {
                if (currentCourse == courseChoice) {
                    return rs.getInt("course_id");
                }
                currentCourse++;
            }
        }
        return -1; // Course not found
    }

    // Method to get topic ID by choice and course ID
    private static int getTopicIdByChoice(Connection conn, int topicChoice, int courseId) throws SQLException {
        String query = "SELECT topic_id FROM topic WHERE course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
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
        return -1; // Topic not found
    }

    // Method to get question ID by choice and topic ID
    private static int getQuestionIdByChoice(Connection conn, int questionChoice, int topicId) throws SQLException {
        String query = "SELECT question_id FROM question WHERE topic_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, topicId);
            try (ResultSet rs = pstmt.executeQuery()) {
                int currentQuestion = 1;
                while (rs.next()) {
                    if (currentQuestion == questionChoice) {
                        return rs.getInt("question_id");
                    }
                    currentQuestion++;
                }
            }
        }
        return -1; // Question not found
    }
}
