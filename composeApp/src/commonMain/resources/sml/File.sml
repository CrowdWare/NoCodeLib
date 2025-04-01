 ElementDefinition {
     name: "File"
     description: "It's the file description for a deployment descriptor."

     AllowedRoots {
         Root { name: "Deployment"}
     }

     Properties {
         Property {
             name: "path"
             type: "String"
             default: ""
             description: "The name of a file. Example: home.md"
         }
     }
      Properties {
          Property {
              name: "time"
              type: "String"
              default: ""
              description: "The date time of the file."
          }
      }
      Properties {
          Property {
              name: "type"
              type: "String"
              default: ""
              description: "The type of a file. Example: part-de"
          }
      }
 }