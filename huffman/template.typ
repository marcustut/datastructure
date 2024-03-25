// The project function defines how your document looks.
// It takes your content and some metadata and formats it.
// Go ahead and customize it to your liking!
#let project(
  title: "", 
  authors: (), 
  date: none, 
  font: "linux libertine",
  monofont: "Courier",
  body
) = {
  set document(author: authors.map(a => a.name), title: title)
  set page(margin: 1in, numbering: "1", number-align: center)
  set heading(numbering: "1.1")
  set text(font: font, lang: "en")
  show raw: set text(font: monofont)
  show par: set block(spacing: 1.2em)
  show heading: set block(above: 1.4em, below: 1em)

  // Title row.
  align(center)[#text(1.75em, [*#title*])]

  // Author information.
  pad(
    top: 0.5em,
    x: 2em,
    grid(
      columns: (1fr,) * calc.min(3, authors.len()),
      gutter: 1em,
      ..authors.map(author => align(center)[
        #text(1em, author.name)
        #v(0.5em, weak: true)
        #text(0.9em, author.email)
      ]),
    ),
  )

  // Date.
  align(center)[
    #text(0.9em, date)
  ]

  // Main body.
  set par(leading: 0.55em, first-line-indent: 0em, justify: true)

  body
}