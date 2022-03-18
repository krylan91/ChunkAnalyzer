package me.aakrylov.chunkanalyzer.component;

import lombok.Getter;

@Getter
public class Pair<L, R> {

    private final L left;
    private final R right;

    private Pair(L leftElement, R rightElement) {
        this.left = leftElement;
        this.right = rightElement;
    }

    public static <L, R> Pair<L, R> of(L leftElement, R rightElement) {
        return new Pair<>(leftElement, rightElement);
    }
}
