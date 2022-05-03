package demo;

import java.util.UUID;

public class Book {

    public String isbn;
    String name;
    String author;

    public Book() {
        this.name = UUID.randomUUID().toString();
        this.author = UUID.randomUUID().toString();
        this.isbn = UUID.randomUUID().toString();
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getMediumCover(){
        return name;
    }

    public String getIsbn() {
        return isbn;
    }
}
