package cache;

import com.intellij.psi.PsiMethod;
import utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommentStatusCache {

    private static final CommentStatusCache instance = new CommentStatusCache();
    private final Map<PsiMethod, Boolean> cache = new ConcurrentHashMap<>();

    private CommentStatusCache() {} // Private constructor to ensure Singleton

    // Public static method to access the instance
    public static CommentStatusCache getInstance() {
        return instance;
    }

    public boolean hasComment(PsiMethod method) {
        return cache.computeIfAbsent(method, this::calculateHasComment);
    }

    public void addMethod(PsiMethod method) {
        cache.putIfAbsent(method, calculateHasComment(method));
    }

    private boolean calculateHasComment(PsiMethod method) {
        return Utils.hasComment(method);
    }

    public void invalidateMethodInCache(PsiMethod method) {
        cache.remove(method);
    }

    public void clearCache() {
        cache.clear();
    }

    @Override
    public String toString() {
        return "CommentStatusCache{" +
                "cache=" + cache +
                '}';
    }
}
