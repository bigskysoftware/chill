@param java.util.List<demo.Book> books

<html lang="en">
<head>
    <title>TITLE</title>
    <link rel="stylesheet" href="/public/main.css">
    <link rel="icon" href="/public/favicon.png">
    <meta name="viewport" content="width=device-width, initial-scale=1">
</head>

<body>
<header>
    <nav>
        <a href="/index"><img id="logo" src="/public/img/logo.png" alt="Javalin Library"></a>
        <ul id="chooseLanguage">
            <form>
                <li>
                    <button name="locale" value="de" style="background-image: url(/public/img/german.png);"></button>
                </li>
                <li>
                    <button name="locale" value="en" style="background-image: url(/public/img/english.png);"></button>
                </li>
            </form>
        </ul>
    </nav>
</header>
<main>
    <h1>Foo</h1>
    <div class="row row-3">
        @for(var book : books)
            <div class="col">
                <a class="book" href="/books/${book.isbn}">
                    <div class="bookCover">
                        <img src="${book.getMediumCover()}" alt="${book.getName()}">
                    </div>
                    ${book.getAuthor() + book.getName()}
                </a>
            </div>
        @endfor
    </div>
</main>
<footer>
    Some Footer Stuff
</footer>
</body>

</html>