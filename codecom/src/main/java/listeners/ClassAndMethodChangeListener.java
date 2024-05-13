package listeners;

import cache.CommentStatusCache;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import method.TreeBuilder;
import org.jetbrains.annotations.NotNull;

public class ClassAndMethodChangeListener extends PsiTreeChangeAdapter {
    private final Project project;
    private final TreeBuilder treeBuilder;
    private final CommentStatusCache commentCache;

    public ClassAndMethodChangeListener(Project project, TreeBuilder treeBuilder, CommentStatusCache commentCache) {
        this.project = project;
        this.treeBuilder = treeBuilder;
        this.commentCache = commentCache;
    }

    @Override
    public void childAdded(PsiTreeChangeEvent event) {
        System.out.println("child added");
        PsiElement eventChild = event.getChild();
        PsiElement eventParent = event.getParent();
        System.out.println(eventChild.getClass());
//        if (eventChild instanceof PsiClass || eventChild instanceof PsiMethod) {
//            treeBuilder.addChildToParent(eventChild, eventParent);
//
//            if (eventChild instanceof PsiMethod) {
//                commentCache.addMethod((PsiMethod) eventChild);
//            } else {
//                PsiClass psiClass = (PsiClass) eventChild;
//                for (PsiMethod method : psiClass.getMethods()) {
//                    commentCache.addMethod(method);
//                }
//            }
//        }
    }

    @Override
    public void childRemoved(@NotNull PsiTreeChangeEvent event) {
        System.out.println("child removed");
        PsiElement element = event.getChild();

        System.out.println(element.getClass());
    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent event) {
        System.out.println("child replaced");
        PsiElement element = event.getChild();

        System.out.println(element.getClass());
    }

    @Override
    public void childMoved(@NotNull PsiTreeChangeEvent event) {
        System.out.println("child moved");
        PsiElement element = event.getChild();

        System.out.println(element.getClass());
    }

    @Override
    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
        System.out.println("child changed");
        PsiElement element = event.getElement();

        System.out.println(element.getClass());
    }



    // Optionally override other methods for additional handling
}
