package toolWindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeListener;
import listeners.ClassAndMethodChangeListener;
import methodTree.TreeBuilder;

import javax.swing.*;
import java.awt.*;

import static actions.CommentNodeAction.performActionOnSelectedMethod;
import static utils.PsiElementTreeUtil.repaintTreeAndKeepExpanded;

public class ToolWindowContent extends JPanel implements Disposable{
    private final Project project;
    private final JButton commentButton;
    private final JButton refreshButton;
    private final TreeBuilder treeBuilder;
    private PsiTreeChangeListener psiTreeChangeListener;


    public ToolWindowContent(Project project) {
        this.project = project;
        this.commentButton = new JButton("Comment method");
        this.refreshButton = new JButton(AllIcons.Actions.Refresh);
        this.treeBuilder = new TreeBuilder(project);
        setLayout(new BorderLayout());
        initializeUI();
        initializeListeners();
    }

    private void initializeUI() {

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshButton.addActionListener(e -> rebuildMethodTree());
        JButton button = new JButton(AllIcons.Actions.Refresh);
        button.addActionListener(e -> repaintTreeAndKeepExpanded(treeBuilder.getMethodTree()));
        topPanel.add(refreshButton);
        topPanel.add(button);
        add(topPanel, BorderLayout.NORTH);

        //TreeBuilder treeBuilder = new TreeBuilder(project);// Use the tree model from the previous steps
        add(new JScrollPane(treeBuilder.getMethodTree()), BorderLayout.CENTER);

        // Configure the action to be performed when the button is clicked
        commentButton.addActionListener(e -> performActionOnSelectedMethod(project, treeBuilder.getMethodTree()));
        add(commentButton, BorderLayout.SOUTH); // Add the button to the bottom of the panel
        treeBuilder.buildMethodTree();
    }

    private void initializeListeners() {
        PsiManager psiManager = PsiManager.getInstance(project);
        psiTreeChangeListener = new ClassAndMethodChangeListener(treeBuilder);
        psiManager.addPsiTreeChangeListener(psiTreeChangeListener, this); // Auto-removed when this object is disposed
    }

    private void rebuildMethodTree() {
        // Rebuild the method tree from scratch
        treeBuilder.buildMethodTree();
    }



    @Override
    public void dispose() {
        PsiManager.getInstance(project).removePsiTreeChangeListener(psiTreeChangeListener);
    }
}
