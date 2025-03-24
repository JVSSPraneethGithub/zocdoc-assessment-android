package com.zocdoc.assessment.utils


fun <T> isSorted(list: List<T>, comparator: Comparator<T>): Boolean {
    for (i in 0..<list.size - 1) {
        if (comparator.compare(list[i], list[i + 1]) > 0) {
            return false
        }
    }
    return true
}