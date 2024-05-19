package listeners;

import action.CommentAction;
import cache.CommentStatusCache;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import method.TreeBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassAndMethodChangeListener extends PsiTreeChangeAdapter {
    private final Project project;
    private final TreeBuilder treeBuilder;
    private final CommentStatusCache commentCache;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassAndMethodChangeListener.class);


    public ClassAndMethodChangeListener(Project project, TreeBuilder treeBuilder, CommentStatusCache commentCache) {
        this.project = project;
        this.treeBuilder = treeBuilder;
        this.commentCache = commentCache;
    }

    @Override
    public void childAdded(PsiTreeChangeEvent event) {
        PsiElement eventChild = event.getChild();
        PsiElement eventElement  = event.getElement();
        PsiElement eventParent = event.getParent();
        LOGGER.info("child added - element: {} | child : {} | parent : {}" ,eventElement, eventChild.getClass(), eventParent.getClass());
    }

    @Override
    public void childRemoved(@NotNull PsiTreeChangeEvent event) {
        PsiElement element = event.getChild();
        LOGGER.info("child removed - {}" , element.getClass());
    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent event) {
        PsiElement element = event.getChild();
        LOGGER.info("child replaced - {}" , element.getClass());
    }

    @Override
    public void childMoved(@NotNull PsiTreeChangeEvent event) {
        PsiElement element = event.getChild();
        LOGGER.info("child moved - {}" , element.getClass());
    }

    @Override
    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
        PsiElement element = event.getParent();
        LOGGER.info("children changed - {}" , element.getClass());
    }
}
