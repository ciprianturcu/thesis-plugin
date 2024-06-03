package toolWindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeListener;
import listeners.ClassAndMethodChangeListener;
import method.TreeBuilder;
import model.AbstractTreeNode;
import model.DirectoryNode;
import model.MethodNode;
import service.CommentService;

import javax.swing.*;
import java.awt.*;

public class ToolWindowContent extends JPanel implements Disposable{
    private final Project project;
    private final JButton commentButton;

    private final JButton refreshButton;
    private final TreeBuilder treeBuilder;
    private PsiTreeChangeListener psiTreeChangeListener;


    public ToolWindowContent(Project project) {
        this.project = project;
        this.commentButton = new JButton("Comment method");
        this.treeBuilder = new TreeBuilder(project);
        this.refreshButton = new JButton(AllIcons.Actions.Refresh);
        setLayout(new BorderLayout());
        initializeUI();
        initializeListeners();
    }

    private void initializeUI() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(refreshButton);
        add(topPanel, BorderLayout.NORTH);

        //TreeBuilder treeBuilder = new TreeBuilder(project);// Use the tree model from the previous steps
        add(new JScrollPane(treeBuilder.getMethodTree()), BorderLayout.CENTER);

        // Configure the action to be performed when the button is clicked
        commentButton.addActionListener(e -> performActionOnSelectedMethod(treeBuilder.getMethodTree()));
        add(commentButton, BorderLayout.SOUTH); // Add the button to the bottom of the panel
        treeBuilder.buildMethodTree();

        // Configure the refresh button action
        refreshButton.addActionListener(e -> refreshMethodTree());
    }

    private void initializeListeners() {
        PsiManager psiManager = PsiManager.getInstance(project);
        psiTreeChangeListener = new ClassAndMethodChangeListener(project, treeBuilder);
        psiManager.addPsiTreeChangeListener(psiTreeChangeListener, this); // Auto-removed when this object is disposed
    }

    private void refreshMethodTree() {
        // Rebuild the method tree from scratch
        treeBuilder.buildMethodTree();
    }

    private void performActionOnSelectedMethod(JTree methodTree) {
        // Get the selected node from the tree
        AbstractTreeNode selectedNode = (AbstractTreeNode) methodTree.getLastSelectedPathComponent();
        if (selectedNode != null) {
            // Check if it is a method node before performing the action
            if (selectedNode instanceof MethodNode methodNode) {
                PsiFile psiFile = methodNode.getPsiElement().getContainingFile();
                if (psiFile == null) {
                    return;
                }

                Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
                if (document == null) {
                    return;
                }
                CommentService.getInstance().generateCommentForMethod(project, document, methodNode.getPsiElement());

            }
            if(selectedNode instanceof DirectoryNode directoryNode) {
                System.out.println("Performing action on directory: " + directoryNode.getLabel());
                // Example: You could show the method name in a dialog
                JOptionPane.showMessageDialog(this, "Directory Selected: " + directoryNode.getLabel());
            }

        }
    }

    @Override
    public void dispose() {
        PsiManager.getInstance(project).removePsiTreeChangeListener(psiTreeChangeListener);
    }
}
