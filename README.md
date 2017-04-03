# TabList

### Usage:
_Requires a permission plugin that supports 'Permission Options'_

Permission Option Name: `tablist:format`  
Permission Option Value: `{name}`  
Where `{name}` will be replaced by the user's name. [TextMU strings](https://github.com/dags-/TextMU/wiki/Notation) are supported for color/style formatting.

### Example Using PermissionsEx Commands:
`/pex group admin option add tablist:format [red,bold]({name})`  
_Any user in the 'admin' group will have a bold & red name in the tablist_
