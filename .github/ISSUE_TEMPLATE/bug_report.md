name: Bug report
description: Create a report to help us improve Bhagwa
labels: ["bug"]
body:
  - type: markdown
    attributes:
      value: Thanks for filing a bug report!
  - type: textarea
    id: what-happened
    attributes:
      label: What happened?
    validations:
      required: true
  - type: textarea
    id: expected
    attributes:
      label: Expected behavior
    validations:
      required: true
