# Chill Template Language

The Chill Template Language is Yet Another Templating Language (YATL) for the JVM.  

CTL is designed to be relatively simple and lightweight, it is not overly concerned with pure rendering performance.

## Syntax

Chill Templates consist of a mixture of expressions and commands.  

Here is an example chill template:

```html
#layout layouts/layout.html 

<main>
    <h1>Welcome To Lekker Templates!</h1>
    <h2>Iteration</h2>
    <p>Iteration is done with the #for command:</p>
    <ul>
        #for u in users
          <li>${u.email}</li>
        #end
    </ul>
    <h2>Expressions</h2>
    <p>Expressions are enclosed in &dollar;{}: ${"Hello" + name}</p>
</main>
```

Note that commands start with a `#` and expressions start with a `$`. 

### Commands

All commands in Chill Templates start with a `#` character and *must* start the line they are on.  Commands are
typically entirely on one line:

```html
#for contact in contacts
```

but may span lines if they include an open parenthesis, bracket or brace:

```html
#for number in [1, 
                2,
                3]
```

The core commands in CTL are:

* `#if`, `#elseif`, `#else` - conditional inclusion logic
* `#end` - ends a command block
* `#layout` - specifies a layout template for the current template
* `#for` - iterates
* `#content` - in a layout template, includes the content of the layoutee
* `##` - line comment
* `#fragment` - delimits a template fragment

#### Macros

User-defined macros can be added to CTL and allow you to define a macro 

```html
<div>
  #input(for:user.email label:"Enter your email")
</div>
```

Like commands, macros must be on a line that starts with `#`, followed by the macro name, followed by an open paren.

Arguments to the macro consist of a name and value pair separated by a colon `:`.  Arguments can be optionally separated
by commas.