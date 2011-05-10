About Shept
===========
Shept provides a tiny add-on to spring-mvc for form-based web applications that allow extendible forms.
Instead of writing complete forms with a fixed job you'll write snippets that compose to forms
and that can be rearranged in a simple fashion.

Shept focuses on Data-grids as a default form element for *all create-read-update-delete* form operations 
within the same form that allows an almost code-free initial fully functional protoype of your web application.

In particular you don't need to create different forms for viewing, paging and creating entities - it all happens in one step. 

Shept incorporates ideas from '4GL' frameworks that were popular in the late 90's and that
were famous for building enterprise applications in a rapid prototyping fashion.

Read more in the [Feature documentation][features]

Look at the [Online examples][demos]

Shept implementation basics
===========================

Shept is deeply integrated with spring-mvc and hibernate / hibernate-annotations.

Infrastructure provisions
-------------------------

1. The SheptController supports a notion of springs 'formbackingObject' that's actually an array of objects
2. A layered architecture of *PageableList* *MultiChoice* and *Refreshable* along with their default implementations for datgrid-handling: 
*PageListHolder* *ChoiceListHolder* *FilteredListHolder*
3. A set of utility classes - default configurations - and JSP view templates 

Follow the [Getting started documentation][getting started]

Shept History
=============

Shept has been developed in a couple of web projects since 2008. While early versions of shept were deeply integrated with those custom projects
shept has been made available in early 2011 as a separate library under Apache 2 licence.

Shept is short for

* **S**pring
* **H**ibernate
* **E**clipse
* **P**ostgres
* **T**omcat

While Eclipse, Postgres and Tomcat are not mandatory parts we are referring to those environments in the documentation and the examples.

[features]: http://shept.org/docs/Shept/Features
[demos]: http://shept.org/docs/Demos/
[getting started]: http://shept.org/docs/Demo1/
