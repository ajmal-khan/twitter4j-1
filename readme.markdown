# Customized Twitter4j API

Several functions are added/modified to be used for my personal research.

Original twitter4j git repository: https://github.com/twitter/twitter4j

Original twitter4j javadoc page: http://twitter4j.org/en/javadoc/index.html


# How to Build

## Build the package first

Type:

    make


## Upate the library

Type:

    cd lib
    ./update_library.sh

# What's Modified

## StatusJSONImpl object

Previously this object's toString() function puts invalid json representation,
which occurs during converting json string from twitter api into object's own
private member variables, and coverting them back to json representation.

Now the object simply stores the reference to the original JSONObject, and use
the object directly to print out string representation.


# What's Added

