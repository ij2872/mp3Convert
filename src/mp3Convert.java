import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.DimensionUIResource;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Created by Ivan on 2/18/17.
 * mp3Convert will be used to convert a folder containing WAVE files into mp3 files
 * A new mp3 folder will be created on the same level as the Wave folder
 * The user can select a WAVE file or a folder containing WAVE files
 * -- REQUIRES FFMPEG INSTALLED, MAC ONLY --
 */
public class mp3Convert extends JFrame {
    private File defaultDir = new File("/Users/");
    private boolean isFolderSelected = false;
    private boolean isCurrentlyConverting = false;
    private int result;
    private File[] fileContents;
    private File selectedFolderDir;
    private String workingDir = System.getProperty("user.dir"); // Location of file save
    JLabel status = new JLabel("OFF"); // Change while converting and after

    public mp3Convert() {
        super("MP3 Converter");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        setPreferredSize(new Dimension(300, 200));
        setResizable(false);
        Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) (ss.getWidth() / 2) - 300, (int) (ss.getHeight() / 2) - 200);

        // Chooser for files
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(defaultDir);
        fileChooser.setVisible(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter("WAV Files", "wav"));

        // Chooser for dir
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setCurrentDirectory(defaultDir);
        dirChooser.setVisible(true);
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Chooser for dir of SAVE
        JFileChooser saveDirChooser = new JFileChooser();
        saveDirChooser.setCurrentDirectory(defaultDir);
        saveDirChooser.getSelectedFile();
        saveDirChooser.setVisible(true);
        saveDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);


        // Button file selectors
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLocation(1, 2);
        buttonPanel.setBorder(new BorderUIResource.LineBorderUIResource(Color.lightGray));

        // Create,Add Listener, Add to Panel
        JButton selectFolder = new JButton("Folder");
        JButton selectFiles = new JButton("File");

        // Folder Button Action, if pressed runConversion will be executed
        selectFolder.addActionListener(e -> {
            result = dirChooser.showSaveDialog(super.getParent());
            selectedFolderDir = dirChooser.getSelectedFile();
            isFolderSelected = true;
            runConversion();

        });

        // File Button Action, if pressed runConversion will be executed
        selectFiles.addActionListener(e -> {
            result = fileChooser.showSaveDialog(super.getParent());
            selectedFolderDir = fileChooser.getSelectedFile();
            isFolderSelected = false;
            runConversion();
        });

        // Status Label
        JPanel statusPanel = new JPanel();
        JLabel statusTitle = new JLabel("Status");
        statusPanel.add(statusTitle);
        statusPanel.add(status);

        // Save To Label
        JPanel dirPanel = new JPanel(new GridBagLayout());
        JLabel dirTextLabel = new JLabel("Save To ");
        JTextField dirTextField = new JTextField(20);
        JButton dirChanger = new JButton("...");


        // adjust dirPanel properties
        dirTextField.setText(workingDir.toString());
        dirTextField.setEnabled(false);
        dirChanger.addActionListener(e -> {
            result = saveDirChooser.showSaveDialog(super.getParent());
            workingDir = saveDirChooser.getSelectedFile().toString();
            System.out.println(workingDir);
            dirTextField.setText(workingDir.toString());
        });

        //gridBagLayout -
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.LINE_START;
        dirPanel.add(dirTextLabel, c);

        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        dirPanel.add(dirTextField, c);

        c.gridx++;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        //dirPanel.add(dirChanger, c);

        // Add components
        buttonPanel.add(selectFiles);
        buttonPanel.add(selectFolder);
        add(statusPanel, BorderLayout.NORTH);
        add(dirPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
    }


    // Changes status JLabel
    public void setStatus(String status) {
        this.status.setText(status);
        super.update(getGraphics());
    }

    // Returns the files extension as a string
    public String getExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * @param file location of file of wav
     * @return filepath/{name}.mp3
     */
    public String mp3Namer(File file) {
        return file.getName().substring(0, file.getName().lastIndexOf("."));
    }

    // Runs a ProcessBuilder using ffmpeg
    public static void terminalEncoderTest(File source, File dest) {
        // EX: ffmpeg -i (Source) -vn -ar 44100 -ac 2 -b:a 128k -f mp3 (Target)
        String sourcePath = source.getAbsolutePath();
        String destPath = dest.getAbsolutePath();

        try {
            Process p = new ProcessBuilder("ffmpeg", "-i",
                    sourcePath,
                    "-vn",
                    "-ar", "44100",
                    "-ac", "2",
                    "-b:a", "128k",
                    "-f", "mp3",
                    destPath)
                    .start();

            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader buff = new BufferedReader(isr);

            String line;
            while ((line = buff.readLine()) != null)
                System.out.println(line);

            p.isAlive();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    // Converts a folder of WAVs to mp3s
    public void folderConversion() {
        fileContents = selectedFolderDir.getParentFile().listFiles();

        // Create target folder
        File targetDir = new File(workingDir + "/mp3s");
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }

        // convert wav of each file in fileContents
        for (File file : fileContents) {

            // Find extension for wav files only
            if ("wav".equals(getExtension(file))) {
                File target = new File(targetDir + "/" + mp3Namer(file) + ".mp3");
                System.out.println(target.getName());
                setStatus("...");

                // find if file already exist, if not use ffmpeg converter
                if (!target.exists()) {

                    terminalEncoderTest(file, target);
                    System.out.println("mp3 Created.");
                } else {
                    setStatus("file(s) already exist");
                    System.out.println("File Already Exist");
                }
            }
        }
        System.out.println("Finished File Conversion");
        setStatus("Finished");
    }

    // Converts a single WAV to mp3
    public void fileConversion() {
        File source = selectedFolderDir;

        // Create target folder for mp3
        File targetDir = new File(workingDir + "/mp3s");
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }

        // Check if file is a wav file
        if ("wav".equals(getExtension(source))) {
            // mp3 file being created
            File target = new File(targetDir + "/" + mp3Namer(source) + ".mp3");
            System.out.println(target.getName());
            setStatus("...");

            // find if file already exist, if not use ffmpeg converter
            if (!target.exists()) {
                terminalEncoderTest(source, target);
                System.out.println("mp3 Created");
                System.out.println("Finished File Conversion");
                setStatus("Finished");
            } else {
                setStatus("file(s) already exist");
                System.out.println("File Already Exist");
            }

        }
    }

    // Run MP3 Converter(JAVE)
    private void runConversion() {

        // if folder selected, get files. else convert file
        if (isFolderSelected) {
            folderConversion();
        } else {
            fileConversion();
        }
    }

}
