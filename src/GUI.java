import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class GUI extends JFrame{
    private JButton openFile;
    private JPanel panelMain;
    private JButton computeAllButton;
    private JTextArea fileContentTextArea;
    private JComboBox selectSource;
    private JButton changeSourceButton;
    private JButton singleStepButton;
    private JPanel update;
    private JButton linkBrokenButton;
    private JComboBox brokenSource;
    private JComboBox brokenDest;
    private JButton deleteNodeButton;
    private JComboBox deleteNode;
    private JButton clearButton;
    private JButton saveFileButton;
    private JButton newUpdateNodeButton;
    private JTextField newNodeTextField;
    private JButton undoButton;
    private JButton redoButton;
    private JPanel filePanel;
    private JTextPane statusLine;
    private LSR lsr;
    private List<JComboBox> nodesBox;
    private Stack<String> undoStr = new Stack<>();
    private Stack<String> redoStr = new Stack<>();

    public File file;

    public GUI() {
        setTitle("LSR");
        setContentPane(this.panelMain);
        setSize(1000,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        nodesBox = Arrays.asList(selectSource, brokenSource, brokenDest, deleteNode);

        openFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                JFileChooser fileChooser = new JFileChooser();
                //set file filter of ".lsa"
                fileChooser.setFileFilter(new FileNameExtensionFilter("LSA packet (*.lsa)","lsa"));
                //set current directory to project directory
                fileChooser.setCurrentDirectory(new File("."));
                //open file chooser dialog
                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    file = new File((fileChooser.getSelectedFile().getAbsoluteFile()).toString());
                    try {
                        lsr = new LSR(file);
                        appendToPane(statusLine,"Open \""+file.getName()+"\"\n", Color.GREEN);
                        buttonsEnabler();
                        undoStr_redoStrReset();
                        resetAll();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        //System.out.println(ex);
                        appendToPane(statusLine,ex.getMessage()+" in \""+file.getName()+"\"\n", Color.RED);
                    }
                }
            }
        });

        computeAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                appendToPane(statusLine, lsr.computeAll()+"\n", Color.BLACK);
            }
        });

        changeSourceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lsr.setSource(selectSource.getSelectedIndex());
                appendToPane(statusLine, "Change source node to \""+lsr.getNodeName(selectSource.getSelectedIndex())+"\"\n", Color.GREEN);
            }
        });

        singleStepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                appendToPane(statusLine, lsr.singleStep()+"\n", Color.BLACK);
            }
        });

        linkBrokenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int src = brokenSource.getSelectedIndex();
                int dest = brokenDest.getSelectedIndex();
                if (src == dest){
                    appendToPane(statusLine,"Cannot break link between same node\n", Color.RED);
                    return;
                }
                String output = "Link broken: "+lsr.getNodeName(src)+" > "+lsr.getNodeName(dest);
                pushToUndo(output);
                lsr.delLink(src,dest);
                appendToPane(statusLine,output+"\n", Color.GREEN);
                resetOutput();
            }
        });

        deleteNodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String output = "Delete node: "+lsr.getNodeName(deleteNode.getSelectedIndex());
                pushToUndo(output);
                lsr.delNodes(deleteNode.getSelectedIndex());
                appendToPane(statusLine,output+"\n", Color.GREEN);
                resetAll();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statusLine.setText("");
            }
        });

        saveFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JFileChooser fileChooser = new JFileChooser();
                    //set default filename as current opened lsa file
                    fileChooser.setSelectedFile(getFile());
                    //set file filter of ".lsa"
                    fileChooser.setFileFilter(new FileNameExtensionFilter("LSA packet (*.lsa)","lsa"));
                    //set current directory to project directory
                    fileChooser.setCurrentDirectory(new File("."));
                    if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
                        File fileToSave = fileChooser.getSelectedFile().getAbsoluteFile();
                        lsr.saveToLsa(fileToSave);
                        appendToPane(statusLine,"Save to \""+fileToSave.getName()+"\"\n", Color.GREEN);
                    }
                }catch (Exception ex){
                    appendToPane(statusLine, ex.getMessage()+"\n", Color.RED);
                }
            }
        });

        newUpdateNodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String image = lsr.toString();
                    String output;
                    if (lsr.inputToUpdate(newNodeTextField.getText())){
                        //add new node
                        resetAll();
                        output = "New node added: ";
                    } else {
                        //update node
                        resetOutput();
                        output = "Node updates: ";
                    }
                    output = output + newNodeTextField.getText();
                    appendToPane(statusLine, output+"\n", Color.GREEN);
                    pushToUndo(output, image);
                }catch (Exception ex){
                    appendToPane(statusLine, "Invalid input\n", Color.RED);
                }
            }
        });

        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String output = undo();
                if (output == null) return;
                appendToPane(statusLine, output+"\n", Color.GREEN);
                resetAll();
                undo_redoButtonsEnabler();
            }
        });

        redoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String output = redo();
                if (output == null) return;
                appendToPane(statusLine, output+"\n", Color.GREEN);
                resetAll();
                undo_redoButtonsEnabler();
            }
        });
    }

    /**
     * Push the String of current topology update to undoStr and image of current network topology to undoStack in lsr
     * @param output String of current topology update
     */
    private void pushToUndo(String output){
        //push topology update output to undo string stack
        undoStr.push(output);
        undoButton.setEnabled(true);
        //clear the redo string stack
        redoStr.clear();
        lsr.pushToUndo();
    }

    /**
     * Push the String of current topology update to undoStr and image of network topology to undoStack in lsr
     * @param output String of current topology update
     */
    private void pushToUndo(String output, String image){
        //push topology update output to undo string stack
        undoStr.push(output);
        undoButton.setEnabled(true);
        //clear the redo string stack
        redoStr.clear();
        lsr.pushToUndo(image);
    }

    /**
     * Performing undo the topology update
     * @return String of undo topology update
     */
    private String undo(){
        if (undoStr.isEmpty()){
            return null;
        }
        String output = undoStr.pop();
        redoStr.push(output);
        redoButton.setEnabled(true);
        lsr.undo();
        return "undo " + output;
    }

    /**
     * Performing redo the topology update
     * @return String of redo topology update
     */
    private String redo(){
        if (redoStr.isEmpty()) {
            return null;
        }
        String output = redoStr.pop();
        undoStr.push(output);
        lsr.redo();
        return "redo " + output;
    }

    /**
     * Enable the buttons that need to open a LSA packet first
     */
    private void buttonsEnabler(){
        saveFileButton.setEnabled(true);
        computeAllButton.setEnabled(true);
        singleStepButton.setEnabled(true);
        changeSourceButton.setEnabled(true);
        linkBrokenButton.setEnabled(true);
        deleteNodeButton.setEnabled(true);
        newUpdateNodeButton.setEnabled(true);
    }

    /**
     * Enable undo and redo buttons by checking undo and redo function are enable
     */
    private void undo_redoButtonsEnabler(){
        undoButton.setEnabled(!undoStr.isEmpty());
        redoButton.setEnabled(!redoStr.isEmpty());
    }

    private void undoStr_redoStrReset(){
        undoStr.clear();
        redoStr.clear();
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
    }

    private File getFile(){ return file; }

    /**
     * Append colored words to JTextPane
     * @param tp JTextPane to append
     * @param msg String to output
     * @param c color of output word
     * Reference https://stackoverflow.com/questions/9650992/how-to-change-text-color-in-the-jtextarea
     */
    private void appendToPane(JTextPane tp, String msg, Color c)
    {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
    }

    /**
     * This method resets all ComboBoxes.
     */
    private void resetCombobox(){ //reset all combo boxes
        for (JComboBox jComboBox : nodesBox) {
            jComboBox.removeAllItems();
            for (String nodeName : lsr.getNodeList()){
                jComboBox.addItem(nodeName);
            }
        }
    }

    /**
     * This method resets the file content text area (Current nodes and links:).
     */
    private void resetOutput() { //reset the file content text area (Current nodes and links:)
        fileContentTextArea.setText(lsr.toString());
    }

    /**
     * This method resets all ComboBoxes and file content text area.
     */
    private void resetAll(){ //reset both combo boxes and file content text area
        resetCombobox();
        resetOutput();
    }
}
