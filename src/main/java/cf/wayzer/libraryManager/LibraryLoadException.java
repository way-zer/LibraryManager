package cf.wayzer.libraryManager;

class LibraryLoadException extends Exception {
    LibraryLoadException(String message) {
        super(message);
    }

    LibraryLoadException(String message, Exception e) {
        super(message, e);
    }
}
