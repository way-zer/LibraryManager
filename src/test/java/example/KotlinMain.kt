package example

import cf.wayzer.libraryManager.LibraryManager

//You can't use kotlin Function or Type before loadKotlinStd()
//val v = listOf(123)
fun main() {
    LibraryManager.loadKotlinStd()
    // Now can use kotlin
    listOf(123)
}