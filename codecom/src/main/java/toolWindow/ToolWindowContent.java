package toolWindow;

import com.intellij.openapi.project.Project;
import method.TreeBuilder;
import model.TreeNodeData;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;

public class ToolWindowContent extends JPanel {
    private final Project project;
    private final JButton commentButton;

    public ToolWindowContent(Project project) {
        this.project = project;
        this.commentButton = new JButton("Comment method");
        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        TreeBuilder treeBuilder = new TreeBuilder(project);// Use the tree model from the previous steps
        add(new JScrollPane(treeBuilder.getMethodTree()), BorderLayout.CENTER);

        // Configure the action to be performed when the button is clicked
        commentButton.addActionListener(e -> performActionOnSelectedMethod(treeBuilder.getMethodTree()));
        add(commentButton, BorderLayout.SOUTH); // Add the button to the bottom of the panel
        treeBuilder.buildMethodTree();
    }

    private void performActionOnSelectedMethod(JTree methodTree) {
        // Get the selected node from the tree
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) methodTree.getLastSelectedPathComponent();
        if (selectedNode != null && selectedNode.getUserObject() instanceof TreeNodeData) {
            TreeNodeData nodeData = (TreeNodeData) selectedNode.getUserObject();
            // Check if it is a method node before performing the action
            if (nodeData.getType() == TreeNodeData.NodeType.METHOD) {
                // Perform your desired action here
                System.out.println("Performing action on method: " + nodeData.getLabel());
                // Example: You could show the method name in a dialog
                JOptionPane.showMessageDialog(this, "Method Selected: " + nodeData.getLabel());
            }
        }
    }
}
