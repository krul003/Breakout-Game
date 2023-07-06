import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BreakoutGame extends JFrame {
    private static final int WIDTH = 900;
    private static final int HEIGHT = 600;
    private static final int PADDLE_WIDTH = 100;
    private static final int PADDLE_HEIGHT = 20;
    private static final int BALL_SIZE = 20;
    private static final int BRICK_WIDTH = 80;
    private static final int BRICK_HEIGHT = 20;
    private static final int ROWS = 5;
    private static final int COLS = 10;
    private static final int DELAY = 10;

    private JPanel gamePanel;
    private Timer gameTimer;

    private int paddleX;
    private int ballX;
    private int ballY;
    private double ballXDir;
    private double ballYDir;
    private boolean isPlaying;
    private int score;

    private int[][] bricks;

    public BreakoutGame() {
        setTitle("Breakout Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        initializeGame();

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGame(g);
            }
        };

        gamePanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        gamePanel.setBackground(Color.BLACK);
        add(gamePanel);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e);
            }
        });

        setFocusable(true);
        requestFocus();
    }

    private void initializeGame() {
        paddleX = WIDTH / 2 - PADDLE_WIDTH / 2;
        ballX = WIDTH / 2 - BALL_SIZE / 2;
        ballY = HEIGHT / 2 - BALL_SIZE / 2;
        ballXDir = 3.0;
        ballYDir = -3.0;
        isPlaying = true;
        score = 0;

        bricks = new int[ROWS][COLS];
        generateBricks();

        gameTimer = new Timer(DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateGame();
                gamePanel.repaint();
            }
        });
        gameTimer.start();
    }

    private void generateBricks() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                bricks[i][j] = 1;
            }
        }
    }

    private void drawGame(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Score
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Score: " + score, 10, 20);

        // Draw paddle
        GradientPaint paddleGradient = new GradientPaint(
                paddleX, HEIGHT - PADDLE_HEIGHT - 100, Color.WHITE,
                paddleX, HEIGHT - PADDLE_HEIGHT - 100 + PADDLE_HEIGHT / 2, Color.WHITE
        );
        g2d.setPaint(paddleGradient);
        g2d.fillRect(paddleX, HEIGHT - PADDLE_HEIGHT - 100, PADDLE_WIDTH, PADDLE_HEIGHT);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(paddleX, HEIGHT - PADDLE_HEIGHT - 100, PADDLE_WIDTH, PADDLE_HEIGHT);

        // Draw ball
        int ballGradientRadius = BALL_SIZE / 2;
        int ballGradientX = ballX + ballGradientRadius;
        int ballGradientY = ballY + ballGradientRadius;
        Color startColor = Color.RED;
        Color endColor = new Color(255, 51, 51); // Red
        RadialGradientPaint ballGradient = new RadialGradientPaint(
                ballGradientX, ballGradientY, ballGradientRadius,
                new float[]{0f, 1f},
                new Color[]{startColor, endColor}
        );
        g2d.setPaint(ballGradient);
        g2d.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);

        // Draw bricks
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (bricks[i][j] == 1) {
                    int brickX = j * BRICK_WIDTH + 50;
                    int brickY = i * BRICK_HEIGHT + 50;
                    GradientPaint gradient = new GradientPaint(
                            brickX, brickY, Color.LIGHT_GRAY,
                            brickX + BRICK_WIDTH, brickY + BRICK_HEIGHT, Color.DARK_GRAY
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRect(brickX, brickY, BRICK_WIDTH, BRICK_HEIGHT);
                    g2d.setColor(Color.WHITE);
                    g2d.drawRect(brickX, brickY, BRICK_WIDTH, BRICK_HEIGHT);
                }
            }
        }

        // Game over
        if (!isPlaying) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            FontMetrics fm = g2d.getFontMetrics();
            String gameOverMsg = "Game Over";
            int x = (WIDTH - fm.stringWidth(gameOverMsg)) / 2;
            int y = HEIGHT / 2;
            g2d.drawString(gameOverMsg, x, y);
        }
    }

    private void updateGame() {
        if (isPlaying) {
            // Move ball
            ballX += ballXDir;
            ballY += ballYDir;
    
            // Collision detection with paddle
            if (ballY + BALL_SIZE >= HEIGHT - PADDLE_HEIGHT - 100 && ballX + BALL_SIZE >= paddleX && ballX <= paddleX + PADDLE_WIDTH) {
                ballYDir = -ballYDir;
            }
    
            // Collision detection with bricks
            boolean bricksExist = false; // Flag to check if any bricks are left
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    if (bricks[i][j] == 1) {
                        bricksExist = true;
                        int brickX = j * BRICK_WIDTH + 50;
                        int brickY = i * BRICK_HEIGHT + 50;
    
                        if (ballX + BALL_SIZE >= brickX && ballX <= brickX + BRICK_WIDTH && ballY + BALL_SIZE >= brickY && ballY <= brickY + BRICK_HEIGHT) {
                            bricks[i][j] = 0;
                            ballYDir = -ballYDir;
                            score += 10;
                        }
                    }
                }
            }
    
            // Check if all bricks are destroyed
            if (!bricksExist) {
                // Create new bricks
                createNewBricks();
                // Increase ball speed slightly
                increaseBallSpeed();
            }
    
            // Collision detection with walls
            if (ballX <= 0 || ballX + BALL_SIZE >= WIDTH) {
                ballXDir = -ballXDir;
            }
    
            if (ballY <= 0) {
                ballYDir = -ballYDir;
            }
    
            // Check game over
            if (ballY + BALL_SIZE >= HEIGHT) {
                isPlaying = false;
            }
        }
    }
    
    private void createNewBricks() {
        bricks = new int[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                bricks[i][j] = 1;
            }
        }
    }
    
    private void increaseBallSpeed() {
        if (ballXDir > 0) {
            ballXDir += 0.5;
        } else {
            ballXDir -= 0.5;
        }
    
        if (ballYDir > 0) {
            ballYDir += 0.5;
        } else {
            ballYDir -= 0.5;
        }
    }
    

    private void handleKeyPress(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            if (paddleX > 0) {
                paddleX -= 30;
            }
        }

        if (key == KeyEvent.VK_RIGHT) {
            if (paddleX < WIDTH - PADDLE_WIDTH - 15) {
                paddleX += 30;
            }
        }
    }

    private void handleKeyRelease(KeyEvent e) {
        // Do nothing for key release events
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new BreakoutGame().setVisible(true);
            }
        });
    }
}
