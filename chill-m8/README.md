# `m8` - A Web Test Automation Language

`m8` is a scripting language designed to make it easy to create and run end to end web tests.  It works
with [selenium](https://www.selenium.dev/) to make it simple and fun to write tests against a website,
complete with assertions and actions.

`m8` scripts are stored in files named `m8`, and can be run via the command line with the included
runner.

## Syntax

`m8` is an [xTalk]()-style language that reads like english:

```appletalk
  go to https://mysite.com
  enter "user1" into the input labeled "Username:"
  enter "example" into the input labeled "Password:"
  click the "Login" button
  verify the page contains "Welcome!"
```

### Tokens

| type          | examples            |
|---------------|---------------------|
| symbol        | foo                 |
| string        | 'foo', "foo"        |
| class literal | .foo                |
| id            | #foo                |
| xpath         | //img[@alt='foo']   |
| absolute url  | https://example.com |
| relative url  | /index.html         |
| number        | 1.234               |
| operators     | + - /               |

```

### Commands


## Running a m8 script

To run `m8` scripts, you can use the runner included in the distribution:

```shell
$ java -jar m8.jar path/to/myscript.m8
```
