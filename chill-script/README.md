# Lekker Template Language

The Lekker Template Language (LTL) is Yet Another Templating Language (YATL) for the JVM.  

LTL is designed to be relatively simple and lightweight, but is not overly concerned with pure rendering performance.  
Rather it is focused on developer productivity with:

* Automatic detection of developer mode, with hot reload
* Good parse error messages
* Good runtime error messages
* An easy-to-configure setup
* A high level language with features that make producing correct HTML easy

## Syntax

Lekker Templates consist of a mixture of expressions and commands.  

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

All commands in LTL start with a `#` character and *must* start the line they are on.  

Any characters placed before a `#` make that line not a command.

The core commands in LTL are:

* `#if`, `#elseif`, `#else` - conditional inclusion logic
* `#end` - ends a command block
* `#layout` - specifies a layout template for the current template
* `#for` - iterates
* `#content` - in a layout template, includes the content of the layoutee
* `##` - line comment
* `#fragment` - delimits a template fragment

