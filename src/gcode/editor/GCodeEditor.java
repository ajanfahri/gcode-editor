import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GCodeEditor extends JFrame {

    public GCodeEditor() {
        setTitle("G-Code Editörü");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        GCodeTextPane textPane = new GCodeTextPane();
        
         // JScrollPane'e hem textPane hem de lineNumberArea ekleyin
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setRowHeaderView(textPane.lineNumberArea); 
        add(scrollPane);

        //add(new JScrollPane(textPane));

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GCodeEditor());
    }
}

class GCodeTextPane extends JTextPane {

    private static final Pattern GCODE_PATTERN = Pattern.compile("[GMXYZFIJKSPTUVABCE]+[-+]?[0-9]*\\.?[0-9]*");
    private static final SimpleAttributeSet ERROR_ATTRIBUTES = new SimpleAttributeSet();

    static {
        StyleConstants.setForeground(ERROR_ATTRIBUTES, Color.RED);
    }

    public JTextPane  lineNumberArea;
    
    public GCodeTextPane() {
        // Font ayarları
        Font font = new Font("Consolas", Font.PLAIN, 28); // Okunaklı bir font (Consolas)
        setFont(font);
        
        // Font rengi ayarı
        //setForeground(new Color(204, 204, 204)); // Açık gri renk

        // Satır numaraları alanı oluşturma ve ekleme
        lineNumberArea = new JTextPane();
        lineNumberArea.setEditable(false);
        lineNumberArea.setFont(new Font("Consolas", Font.PLAIN, 28));
        lineNumberArea.setBackground(new Color(220, 220, 220)); // Açık gri arka plan
        lineNumberArea.setForeground(Color.LIGHT_GRAY);
        
        // Satır numaralarını güncelle
        updateLineNumbers();

        JScrollPane scrollPane = new JScrollPane(this);
        scrollPane.setRowHeaderView(lineNumberArea);
        
        // DocumentFilter ekleyin
        ((AbstractDocument) getDocument()).setDocumentFilter(new UppercaseDocumentFilter());
    
         this.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                highlightErrors();
                
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                highlightErrors();
                
            }

            @Override
            public void changedUpdate(DocumentEvent e) {}
        });
    }
    
    private void updateLineNumbers() {
    
        int lineCount = getDocument().getDefaultRootElement().getElementCount();
        StringBuilder lineNumbersText = new StringBuilder();
        for (int i = 1; i <= lineCount; i++) {
            lineNumbersText.append(i).append("\n");
        }
        lineNumberArea.setText(lineNumbersText.toString());
    
    }

    private void highlightErrors() {
    SwingUtilities.invokeLater(() -> {
        StyledDocument doc = getStyledDocument();
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        doc.setCharacterAttributes(0, doc.getLength(), defaultStyle, true);

        try {
            String text = getText(0, doc.getLength());
            System.out.println(text);
            Matcher matcher = GCODE_PATTERN.matcher(text);
            int lastEnd = 0;
            while (matcher.find()) {
                String code = matcher.group();
                System.out.println("COde : "+text);
                if (!isValidGCode(code)) {
                    doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), ERROR_ATTRIBUTES, false);
                }
                lastEnd = matcher.end();
            }
            doc.insertString(lastEnd, "", null); // İmleci son konuma taşı
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    });
    updateLineNumbers();
    }

    private boolean isValidGCode(String code) {
        // G-code geçerlilik kontrolü burada yapılacak
        return code.matches("[GMXYZF]+[-+]?[0-9]*\\.?[0-9]*"); 
    }
    
    class UppercaseDocumentFilter extends DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        super.insertString(fb, offset, string.toUpperCase(), attr); // Büyük harfe çevir
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        super.replace(fb, offset, length, text.toUpperCase(), attrs); // Büyük harfe çevir
    }
}
 
}