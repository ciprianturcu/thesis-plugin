package toolWindow;

import cache.CommentStatusCache;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeListener;
import listeners.ClassAndMethodChangeListener;
import method.TreeBuilder;
import model.AbstractTreeNode;
import model.DirectoryNode;
import model.MethodNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public class ToolWindowContent extends JPanel implements Disposable{
    private final Project project;
    private final JButton commentButton;
    private final TreeBuilder treeBuilder;
    private PsiTreeChangeListener psiTreeChangeListener;


    public ToolWindowContent(Project project) {
        this.project = project;
        this.commentButton = new JButton("Comment method");
        this.treeBuilder = new TreeBuilder(project);
        setLayout(new BorderLayout());
        initializeUI();
        initializeListeners();
    }

    private void initializeUI() {
        //TreeBuilder treeBuilder = new TreeBuilder(project);// Use the tree model from the previous steps
        add(new JScrollPane(treeBuilder.getMethodTree()), BorderLayout.CENTER);

        // Configure the action to be performed when the button is clicked
        commentButton.addActionListener(e -> performActionOnSelectedMethod(treeBuilder.getMethodTree()));
        add(commentButton, BorderLayout.SOUTH); // Add the button to the bottom of the panel
        treeBuilder.buildMethodTree();
    }

    private void initializeListeners() {
        PsiManager psiManager = PsiManager.getInstance(project);
        psiTreeChangeListener = new ClassAndMethodChangeListener(project, treeBuilder, null);
        psiManager.addPsiTreeChangeListener(psiTreeChangeListener, this); // Auto-removed when this object is disposed
    }

    private void performActionOnSelectedMethod(JTree methodTree) {
        // Get the selected node from the tree
        AbstractTreeNode selectedNode = (AbstractTreeNode) methodTree.getLastSelectedPathComponent();
        if (selectedNode != null) {
            // Check if it is a method node before performing the action
            if (selectedNode instanceof MethodNode methodNode) {
                // Perform your desired action here
                System.out.println("Performing action on method: " + methodNode.getLabel());
                // Example: You could show the method name in a dialog
                JOptionPane.showMessageDialog(this, "Method Selected: " + methodNode.getLabel());

                System.out.println(CommentStatusCache.getInstance().toString());
            }
            if(selectedNode instanceof DirectoryNode directoryNode) {
                System.out.println("Performing action on directory: " + directoryNode.getLabel());
                // Example: You could show the method name in a dialog
                JOptionPane.showMessageDialog(this, "Directory Selected: " + directoryNode.getLabel());

                System.out.println(CommentStatusCache.getInstance().toString());
            }

        }
    }

    @Override
    public void dispose() {
        PsiManager.getInstance(project).removePsiTreeChangeListener(psiTreeChangeListener);
    }
}
