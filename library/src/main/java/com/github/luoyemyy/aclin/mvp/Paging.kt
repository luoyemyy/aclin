package com.github.luoyemyy.aclin.mvp

interface Paging {

    fun reset()

    fun next()

    fun current(): Long

    fun size(): Int

    fun errorBack()

    class Page(private val size: Int = 10) : Paging {

        private var currentPage: Long = 1
        private var prevPage: Long = 0

        override fun reset() {
            prevPage = currentPage
            currentPage = 1
        }

        override fun next() {
            prevPage = currentPage
            currentPage++
        }

        override fun errorBack() {
            if (prevPage > 0) {
                currentPage = prevPage
                prevPage = 0
            }
        }

        override fun size(): Int {
            return size
        }

        override fun current(): Long {
            return currentPage
        }
    }

}