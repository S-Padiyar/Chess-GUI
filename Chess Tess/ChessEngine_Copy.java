import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ChessEngine_Copy extends JFrame {
    private JPanel[][] squares;
    private JPanel contentPane;
    private boolean check = false;
    private KButton changeButton;
    private boolean turn; // white is false, black is true
    private KButton draggedButton = null;
    private Point initialClick;
    
    public static void main(String[] args) {
        ChessEngine_Copy game = new ChessEngine_Copy();
        game.setVisible(true);
    }

    ChessEngine_Copy() {
        setTitle("Chess Engine 2");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(0, 0, 815, 839); // Specifications for windows

        contentPane = new JPanel();
        contentPane.setLayout(null);

        generateSquares();
        placePieces();
        
        setContentPane(contentPane);
    }

    public void placePieces() {
        squares[0][0].add(makePiece("white_rook", 0, 0));
        squares[0][1].add(makePiece("white_knight", 0, 1));
        squares[0][2].add(makePiece("white_bishop", 0, 2));
        squares[0][3].add(makePiece("white_queen", 0, 3));
        squares[0][4].add(makePiece("white_king", 0, 4));
        squares[0][5].add(makePiece("white_bishop", 0, 5));
        squares[0][6].add(makePiece("white_knight", 0, 6));
        squares[0][7].add(makePiece("white_rook", 0, 7));
        for(int j = 0; j < 8; j++) {
            squares[1][j].add(makePiece("white_pawn", 1, j));
        }
        squares[7][0].add(makePiece("black_rook", 7, 0));
        squares[7][1].add(makePiece("black_knight", 7, 1));
        squares[7][2].add(makePiece("black_bishop", 7, 2));
        squares[7][3].add(makePiece("black_queen", 7, 3));
        squares[7][4].add(makePiece("black_king", 7, 4));
        squares[7][5].add(makePiece("black_bishop", 7, 5));
        squares[7][6].add(makePiece("black_knight", 7, 6));
        squares[7][7].add(makePiece("black_rook", 7, 7));

        for(int j = 0; j < 8; j++) {
            squares[6][j].add(makePiece("black_pawn", 6, j));
        }

        for(int i = 2; i < 6; i++) {
            for(int j = 0; j < 8; j++) {
                squares[i][j].add(makePiece("dummy_space", i, j));
            }
        }

        for(int i = 0; i < 8; i++) {
            JLabel temp = new JLabel(i + 1 + "");
            temp.setBounds(2, 2, 22, 22);
            temp.setFont(new Font("SansSerif", Font.BOLD, 20));
            if(i % 2 == 1) {
                temp.setForeground(new Color(74, 103, 65));
            }
            else {
                temp.setForeground(new Color(238, 238, 210));
            }
            squares[i][0].add(temp);
        }

        for(int j = 0; j < 8; j++) {
            JLabel temp = new JLabel((char) (j + 'a') + "");
            temp.setBounds(78, 76, 22, 24);
            temp.setFont(new Font("SansSerif", Font.BOLD, 20));
            if(j % 2 == 1) {
                temp.setForeground(new Color(74, 103, 65));
            }
            else {
                temp.setForeground(new Color(238, 238, 210));
            }
            squares[0][j].add(temp);
            squares[0][j].setComponentZOrder(temp, 0);
        }
    }

    public JButton makePiece(String a, int x, int y) {
        KButton button = new KButton(x, y);
        if(!a.equals("dummy_space")) {
            button.setIcon(loadImage("Images/" + a + ".png"));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
 
        button.setBounds(0, 0, 100, 100);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);   
        button.setFocusPainted(false);
        button.setName(a);

        button.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                buttonLogic(button);
                draggedButton = button;
                initialClick = e.getPoint();
                getComponentAt(initialClick);
                contentPane.setComponentZOrder(button, 0); // Bring the button to the front
                contentPane.repaint();
            }

            public void mouseReleased(MouseEvent e) {
                if (draggedButton != null) {
                    Component c = contentPane.getComponentAt(e.getPoint());
                    if (c instanceof JPanel) {
                        JPanel targetSquare = (JPanel) c;
                        if (checkIfLegalMove(draggedButton)) {
                            swap(draggedButton, targetSquare);
                        }
                    }
                    draggedButton = null;
                }
            }
        });

        button.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (draggedButton != null) {
                    int x = draggedButton.getX() + e.getX() - initialClick.x;
                    int y = draggedButton.getY() + e.getY() - initialClick.y;
                    draggedButton.setLocation(x, y);
                    System.out.println("dragging " + draggedButton.getName());
                }
            }
        });
        
        return button;
    }

    public void buttonLogic(KButton button) {
        System.out.println(button.getName());
        if(!button.getName().equals("dummy_space") && (!check || button.getName().charAt(0) == changeButton.getName().charAt(0))) {
            if (changeButton != null) {
                setButtonColor(changeButton);
            }
            if (changeButton == null || button.xPosition != changeButton.xPosition || button.yPosition != changeButton.yPosition) {
                squares[button.xPosition][button.yPosition].setBackground(new Color(255, 0,0));
                changeButton = button;
                // addDots();
                check = true;
            } else {
                changeButton = null;
                check = false;
            }
            // System.out.println(a);
        } else if (check && button.getName().charAt(0) != changeButton.getName().charAt(0) && checkIfLegalMove(button)) {
            swap(button);
            check = false;
            // System.out.println("swapped");
        }
    }

    public boolean checkIfLegalMove(KButton button) {
        String name = changeButton.getName();

        if (name.equals("white_pawn")) {
            if (button.getName().startsWith("black")) {
                return checkIfCapture(button);
            } else {
                boolean a = button.yPosition == changeButton.yPosition;
                boolean b = button.xPosition - changeButton.xPosition == 1;
                boolean c = changeButton.xPosition == 1 && button.xPosition - changeButton.xPosition == 2;
                // System.out.println(a && (b || c));
                return a && (b || c);
            }
        }
        if (name.equals("black_pawn")) {
            if (button.getName().startsWith("white")) {
                return checkIfCapture(button);
            } else {
                boolean a = button.yPosition == changeButton.yPosition;
                boolean b = button.xPosition - changeButton.xPosition == -1;
                boolean c = changeButton.xPosition == 6 && button.xPosition - changeButton.xPosition == -2;
                // System.out.println(a && (b || c));
                return a && (b || c);
            }
        }
        if (name.endsWith("rook")) {
            if (button.xPosition == changeButton.xPosition)
                return checkIfPiecesInBetween_UPDOWN(button);
            else if (button.yPosition == changeButton.yPosition)
                return checkIfPiecesInBetween_RIGHTLEFT(button);
            else
                return false;
        }
        if (name.endsWith("bishop")) {
            if (Math.abs(button.yPosition-changeButton.yPosition) == Math.abs(button.xPosition-changeButton.xPosition))
                return checkIfPiecesInBetween_DIAGONAL(button);
            else
                return false;
        }
        if (name.endsWith("queen")) {
            if (button.xPosition == changeButton.xPosition)
                return checkIfPiecesInBetween_UPDOWN(button);
            else if (button.yPosition == changeButton.yPosition)
                return checkIfPiecesInBetween_RIGHTLEFT(button);
            else if (Math.abs(button.yPosition-changeButton.yPosition) == Math.abs(button.xPosition-changeButton.xPosition))
                return checkIfPiecesInBetween_DIAGONAL(button);
            else
                return false;
        }
        if (name.endsWith("king")) {
            return Math.abs(button.xPosition - changeButton.xPosition) < 2 && Math.abs(button.yPosition - changeButton.yPosition) < 2;
        }
        if (name.endsWith("knight")) {
            throw new Error("not implemented");
        }

        return true;
    }

    public boolean checkIfPiecesInBetween_UPDOWN(KButton button) {
        int i = changeButton.yPosition;
        int step = (int) Math.signum(button.yPosition - changeButton.yPosition);
        i += step;

        while (i != button.yPosition) {
            if (!squares[button.xPosition][i].getComponent(0).getName().equals("dummy_space")) {
                return false;
            }
            i += step;
        }

        return true;
    }
    
    public boolean checkIfPiecesInBetween_RIGHTLEFT(KButton button) {
        int i = changeButton.xPosition;
        int step = (int) Math.signum(button.xPosition - changeButton.xPosition);
        i += step;

        while (i != Math.abs(button.xPosition)) {
            if (!squares[i][button.yPosition].getComponent(0).getName().equals("dummy_space")) {
                return false;
            }
            i += step;
        }

        return true;
    }

    public boolean checkIfPiecesInBetween_DIAGONAL(KButton button) {
        int i = changeButton.xPosition;
        int j = changeButton.yPosition;
        int istep = (int) Math.signum(button.xPosition - changeButton.xPosition);
        int jstep = (int) Math.signum(button.yPosition - changeButton.yPosition);
        i += istep;
        j += jstep;

        while (i != Math.abs(button.xPosition)) {
            if (!squares[i][j].getComponent(0).getName().equals("dummy_space")) {
                return false;
            }
            i += istep;
            j += jstep;
        }

        return true;
    }

    public boolean checkIfCapture(KButton button) {
        String name = changeButton.getName();

        if (name.equals("white_pawn")) {
            boolean a = Math.abs(button.yPosition - changeButton.yPosition) == 1;
            boolean b = button.xPosition - changeButton.xPosition == 1;
            boolean c = !button.getName().equals("dummy_space");
            return a && b && c;
        }

        if (name.equals("black_pawn")) {
            boolean a = Math.abs(button.yPosition - changeButton.yPosition) == 1;
            boolean b = button.xPosition - changeButton.xPosition == -1;
            boolean c = !button.getName().equals("dummy_space");
            return a && b && c;
        }        

        return false;
    }

    public void swap(KButton curr) {
        int currX = curr.xPosition;
        int currY = curr.yPosition;
        int prevX = changeButton.xPosition;
        int prevY = changeButton.yPosition;
        
        // Remove the piece from the original square and replace with dummy space
        squares[prevX][prevY].removeAll();
        squares[prevX][prevY].add(makePiece("dummy_space", prevX, prevY));
        
        // Update the piece's position in the new square
        squares[currX][currY].removeAll();
        squares[currX][currY].add(makePiece(changeButton.getName(), currX, currY));
        
        // Revalidate and repaint to immediately reflect changes
        squares[prevX][prevY].revalidate();
        squares[prevX][prevY].repaint();
        squares[currX][currY].revalidate();
        squares[currX][currY].repaint();
        
        // Update changeButton
        changeButton = null;
        setContentPane(contentPane);
    }
    
    public void swap(KButton curr, JPanel targetSquare) {
        int prevX = changeButton.xPosition;
        int prevY = changeButton.yPosition;
        int currX = -1;
        int currY = -1;
    
        // Find the coordinates of the target square
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (squares[i][j] == targetSquare) {
                    currX = i;
                    currY = j;
                    break;
                }
            }
        }
    
        if (currX == -1 || currY == -1) {
            return; // Invalid target square
        }
        
        // Remove the piece from the original square and replace with dummy space
        squares[prevX][prevY].removeAll();
        squares[prevX][prevY].add(makePiece("dummy_space", prevX, prevY));
        
        // Update the piece's position in the new square
        squares[currX][currY].removeAll();
        squares[currX][currY].add(makePiece(changeButton.getName(), currX, currY));
        
        // Revalidate and repaint to immediately reflect changes
        squares[prevX][prevY].revalidate();
        squares[prevX][prevY].repaint();
        squares[currX][currY].revalidate();
        squares[currX][currY].repaint();
        
        // Update changeButton
        changeButton = null;
        setContentPane(contentPane);
    }

    public void generateSquares() {
        squares = new JPanel[8][8];
        
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                squares[i][j] = new JPanel();
                if ((i + j) % 2 == 0) {
                    squares[i][j].setBackground(new Color(74, 103, 65));
                } else {
                    squares[i][j].setBackground(new Color(238, 238, 210));
                }
                squares[i][j].setBounds(j * 100, 700 - i * 100, 100, 100);
                squares[i][j].setLayout(null);
                contentPane.add(squares[i][j]);
            }
        }
    }

    public void setButtonColor(KButton button) {
        if ((button.xPosition + button.yPosition) % 2 == 0) 
            squares[changeButton.xPosition][changeButton.yPosition].setBackground(new Color(74, 103, 65));
        else 
            squares[changeButton.xPosition][changeButton.yPosition].setBackground(new Color(238, 238, 210));
    }
    
    private ImageIcon loadImage(String path){
        Image image = new ImageIcon(this.getClass().getResource(path)).getImage();
        Image scaledImage = image.getScaledInstance(100, 100,  java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }
}

class KButton extends JButton {
    int xPosition;
    int yPosition;

    KButton(int x, int y) {
        super();
        xPosition = x;
        yPosition = y;
    }
}
