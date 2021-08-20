# PrimeFlex
PrimeFlex is a lightweight flex based responsive layout utility optimized for mobile phones, tablets and desktops.


### Getting Started
FlexGrid is a CSS utility based on flexbox. For more information about Flex, visit [A Complete Guide to Flexbox](https://css-tricks.com/snippets/css/a-guide-to-flexbox/). A basic grid is defined by giving a container **p-grid** class and children the **p-col** class. Children of the grid will have the same width and scale according to the width of the parent.

```html
<div class="p-grid">
    <div class="p-col">1</div>
    <div class="p-col">2</div>
    <div class="p-col">3</div>
</div>
```

### Direction
Default direction is "row" and **p-dir-\*** class at the container defines the other possible directions which can be "row reverse", "column" and "column reverse".

```html
<!-- Row Reverse -->
<div class="p-grid p-dir-rev">
    <div class="p-col">1</div>
    <div class="p-col">2</div>
    <div class="p-col">3</div>
</div>

<!-- Column -->
<div class="p-grid p-dir-col">
    <div class="p-col">1</div>
    <div class="p-col">2</div>
    <div class="p-col">3</div>
</div>

<!-- Column Reverse -->
<div class="p-grid p-dir-col-rev">
    <div class="p-col">1</div>
    <div class="p-col">2</div>
    <div class="p-col">3</div>
</div>
```

### 12 Column Grid
FlexGrid includes a 12 column based layout utility where width of a column is defined with the **p-col-{number}** style class. Columns with prefined widths can be used with columns with auto width (p-col) as well.

In the first example below, first column covers the 4 units out of 12 and the rest of the columns share the remaining space whereas in the second example, all three columns have explicit units.

```html
<div class="p-grid">
    <div class="p-col-4">4</div>
    <div class="p-col">1 </div>
    <div class="p-col">1 </div>
    <div class="p-col">1 </div>
    <div class="p-col">1 </div>
    <div class="p-col">1 </div>
    <div class="p-col">1 </div>
    <div class="p-col">1 </div>
    <div class="p-col">1 </div>
</div>

<div class="p-grid">
    <div class="p-col-2">2</div>
    <div class="p-col-6">6</div>
    <div class="p-col-4">4</div>
</div>
```

### Multi Line
When the number of columns exceed 12, columns wrap to a new line.

```html
<div class="p-grid">
    <div class="p-col-6">6</div>
    <div class="p-col-6">6</div>
    <div class="p-col-6">6</div>
    <div class="p-col-6">6</div>
</div>
```

### Fixed Width Column
A column can have a fixed width while siblings having auto width. Apply **p-col-fixed** class to fix a column width.

```html
<div class="p-grid">
    <div class="p-col-fixed" style="width:100px">Fixed</div>
    <div class="p-col">Auto</div>
</div>
```

### Responsive

Responsive layout is achieved by applying breakpoint specific classes to the columns whereas **p-col-\*** define the default behavior for mobile devices with small screens. Four screen sizes are supported with different breakpoints.

* p-sm-\* : min-width 576px
* p-md-\* : min-width 768px
* p-lg-\* : min-width 992px
* p-xl-\* : min-width 1200px

In example below, large screens display 4 columns, medium screens display 2 columns in 2 rows and finally on small devices, columns are stacked.


```html
<div class="p-grid">
    <div class="p-col-12 p-md-6 p-lg-3">A</div>
    <div class="p-col-12 p-md-6 p-lg-3">B</div>
    <div class="p-col-12 p-md-6 p-lg-3">C</div>
    <div class="p-col-12 p-md-6 p-lg-3">D</div>
</div>
```

### Horizontal Alignment

**p-justify-\*** classes are used on the container element to define the alignment along the main axis.

* p-justify-start: (default) Items are packed toward the start line
* p-justify-end : Items are re packed toward to end line
* p-justify-center : Items are centered along the line
* p-justify-between: Items are evenly distributed in the line; first item is on the start line, last item on the end line
* p-justify-around: Items are evenly distributed in the line with equal space around them.
* p-justify-even: Items are distributed so that the spacing between any two items (and the space to the edges) is equal.

```html
<div class="p-grid p-justify-between">
    <div class="p-col-2">2</div>
    <div class="p-col-1">1</div>
    <div class="p-col-4">4</div>
</div>
```

### Vertical Alignment
***p-align-\*** classes are used on the container element to define the alignment along the cross axis.

* p-align-stretch: (default) Stretch to fill the container.
* p-align-start : Cross-start margin edge of the items is placed on the cross-start line
* p-align-end :	Cross-end margin edge of the items is placed on the cross-end line
* p-align-center : Items are centered in the cross-axis
* p-align-baseline : Items are aligned such as their baselines align

```html
<div class="p-grid p-align-center">
    <div class="p-col">4</div>
    <div class="p-col">4</div>
    <div class="p-col">4</div>
</div>
```

Vertical alignment can also be defined at column level with the **p-col-align-\*** classes

* p-col-align-stretch : (default) Stretch to fill the container.
* p-col-align-start : Cross-start margin edge of the items is placed on the cross-start line
* p-col-align-end :	Cross-end margin edge of the items is placed on the cross-end line
* p-col-align-center : Items are centered in the cross-axis
* p-col-align-baseline : Items are aligned such as their baselines align

```html
<div class="p-grid">
    <div class="p-col p-col-align-start">
        <div class="box">4</div>
    </div>
    <div class="p-col p-col-align-center">
        <div class="box">4</div>
    </div>
    <div class="p-col p-col-align-end">
        <div class="box">4</div>
    </div>
</div>
```

### Offset
Offset classes allow defining a left margin on a column to avoid adding empty columns for spacing.

```html
<div class="p-grid">
    <div class="p-col-6 p-offset-3">6</div>
</div>

<div class="p-grid">
    <div class="p-col-4">4 </div>
    <div class="p-col-4 p-offset-4">4</div>
</div>
```

The list of offset classes varying within a range of 1 to 12.

* p-col-offset-\* : All screens
* p-sm-offset-\* : min-width: 576px
* p-md-offset-\* : min-width: 768px
* p-lg-offset-\* : min-width: 992px
* p-xl-offset-\* : min-width: 1200px

### Nested
Columns can be nested to create more complex layouts.

```html
<div class="p-grid">
    <div class="p-col-8">
        <div class="p-grid">
            <div class="p-col-6">
                6
            </div>
            <div class="p-col-6">
                6
            </div>
            <div class="p-col-12">
                12
            </div>
        </div>
    </div>
    <div class="p-col-4">
        4
    </div>
</div>
```

### Gutter
A .5 em padding is applied to each column along with negative margins on the container element, in case you'd like to remove these gutters, apply **.p-nogutter** class to the container. Note that this will not only remove the negative margins of the container, but also the padding of the columns.

```html
<div class="p-grid p-nogutter">
    <div class="p-col">1</div>
    <div class="p-col">2</div>
    <div class="p-col">3</div>
</div>
```

Gutters can also be removed from individual columns with the **.p-col-nogutter** class.
```html
<div class="p-grid">
    <div class="p-col">1</div>
    <div class="p-col p-col-nogutter">2</div>
    <div class="p-col">3</div>
</div>
```
