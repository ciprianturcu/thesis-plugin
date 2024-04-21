import com.intellij.psi.PsiMethod;

import java.util.HashMap;
import java.util.Map;

public class CommentStatusCache {
    private final Map<PsiMethod, Boolean> cache = new HashMap<>();

    public boolean hasComment(PsiMethod method) {
        return cache.getOrDefault(method, false);
    }

    public void updateCache(PsiMethod method, boolean hasComment) {
        cache.put(method, hasComment);
    }

    public void commentAdded(PsiMethod method) {
        updateCache(method, true);
    }

    public void clearCache() {
        cache.clear();
    }

}
