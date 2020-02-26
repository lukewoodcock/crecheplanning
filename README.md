# Automated Creche Planning

## Goal
The aim of this application is to provide a tool that will help a creche with the organisation resources. More specifically, automating the planning of shifts.
### Note
Describing as application or an API is over-egging it. There's one endpoint and it's stateless. It would sit nicely on serverless compute, something like AWS Lambda for example.

### Simple example
If in one week there are 5 days, each day has 3 shifts of equal duration, and there are 3 resources, the assignment of shifts between resources should be such that each resource has 5 shifts.

## Getting started
This is an scala [play](https://www.playframework.com/) application and using java 1.8.

Install sbt (scala build tool) to build and run the application. You can find documentation [here](https://www.scala-sbt.org/). For more info [this](https://alvinalexander.com/scala/sbt-how-to-compile-run-package-scala-project) is useful.

Once you've installed sbt and cloned the project, go to the root folder of the repo and run `sbt test` to check all is OK.

## Deployment
Read this [https://www.playframework.com/documentation/2.8.x/Deploying](https://www.playframework.com/documentation/2.8.x/Deploying)

## Requests
`POST /shifts` - Requires a json payload (see `Example` below) and returns json, specifically a list of shifts with an (not unique) identifier, start and end dates, and an assignee (Family) 

## Model / Request Example
### Overview
The computation takes a collection of `families` with `skills` and `contracts`, and tries to provide `cover` for a collection of `shifts` over any given month. It will also consider any `absences` declared.
*NOTE - I should provide a UML rather than embark on this description

### Example
```json
{
  "skills": [
    "Guard_Moyens",
    "Gym_Moyens",
    "Organise",
    "Guard_Grands",
    "Gym_Grands"
  ],
  "contracts": [
    {
      "id": 0,
      "description": "standard_moyens",
      "globalLimits": {
        "daily": 1,
        "weekly": 3
      },
      "shiftRules": [
        {
          "shiftDefinitionIds": [
            "OPEN",
            "CLOSE"
          ],
          "limits": {
            "daily": 1,
            "weekly": 1
          }
        },
        {
          "shiftDefinitionIds": [
            "MORNING_MOYENS",
            "AFTERNOON_MOYENS",
            "GYM_MOYENS"
          ],
          "limits": {
            "daily": 1,
            "weekly": 2
          }
        }
      ]
    },
    {
      "id": 1,
      "description": "standard_grands",
      "globalLimits": {
        "daily": 1
      },
      "shiftRules": [
        {
          "shiftDefinitionIds": [
            "OPEN",
            "CLOSE"
          ],
          "limits": {
            "daily": 1,
            "weekly": 1
          }
        },
        {
          "shiftDefinitionIds": [
            "MORNING_GRANDS",
            "AFTERNOON_GRANDS",
            "GYM_GRANDS"
          ],
          "limits": {
            "daily": 1,
            "weekly": 2
          }
        }
      ]
    }
  ],
  "families": [
    {
      "id": "EMMA",
      "contractId": 0,
      "name": "EMMA",
      "skills": [
        "Organise",
        "Guard_Moyens",
        "Gym_Moyens"
      ]
    },
    {
      "id": "LAUTARO",
      "contractId": 0,
      "name": "LAUTARO",
      "skills": [
        "Organise",
        "Guard_Moyens",
        "Gym_Moyens"
      ]
    },
    {
      "id": "AIMÉE",
      "contractId": 0,
      "name": "AIMÉE",
      "skills": [
        "Organise",
        "Guard_Moyens",
        "Gym_Moyens"
      ]
    },
    {
      "id": "JULIETTE",
      "contractId": 0,
      "name": "JULIETTE",
      "skills": [
        "Organise",
        "Guard_Moyens",
        "Gym_Moyens"
      ]
    },
    {
      "id": "LOUIS",
      "contractId": 0,
      "name": "LOUIS",
      "skills": [
        "Organise",
        "Guard_Moyens",
        "Gym_Moyens"
      ]
    },
    {
      "id": "ÉLISA",
      "contractId": 0,
      "name": "ÉLISA",
      "skills": [
        "Organise",
        "Guard_Moyens",
        "Gym_Moyens"
      ]
    },
    {
      "id": "MARCEAU",
      "contractId": 0,
      "name": "MARCEAU",
      "skills": [
        "Organise",
        "Guard_Moyens",
        "Gym_Moyens"
      ]
    },
    {
      "id": "LOUISE",
      "contractId": 1,
      "name": "LOUISE",
      "skills": [
        "Organise",
        "Guard_Grands",
        "Gym_Grands"
      ]
    },
    {
      "id": "ROMY",
      "contractId": 1,
      "name": "ROMY",
      "skills": [
        "Organise",
        "Guard_Grands",
        "Gym_Grands"
      ]
    },
    {
      "id": "TIAGO",
      "contractId": 1,
      "name": "TIAGO",
      "skills": [
        "Organise",
        "Guard_Grands",
        "Gym_Grands"
      ]
    },
    {
      "id": "RUBEN",
      "contractId": 1,
      "name": "RUBEN",
      "skills": [
        "Organise",
        "Guard_Grands",
        "Gym_Grands"
      ]
    },
    {
      "id": "RAPHAËL",
      "contractId": 1,
      "name": "RAPHAËL",
      "skills": [
        "Organise",
        "Guard_Grands",
        "Gym_Grands"
      ]
    },
    {
      "id": "SUZANNE",
      "contractId": 1,
      "name": "SUZANNE",
      "skills": [
        "Organise",
        "Guard_Grands",
        "Gym_Grands"
      ]
    }
  ],
  "shifts": [
    {
      "id": "OPEN",
      "category": "ORGANISE",
      "description": "Open crèche",
      "startTime": "08:00:00",
      "endTime": "09:30:00",
      "skillsRequirements": [
        "Organise"
      ]
    },
    {
      "id": "CLOSE",
      "category": "ORGANISE",
      "description": "Close crèche",
      "startTime": "17:00:00",
      "endTime": "18:30:00",
      "skillsRequirements": [
        "Organise"
      ]
    },
    {
      "id": "MORNING_MOYENS",
      "category": "GUARD",
      "description": "Morning guard moyens",
      "startTime": "09:30:00",
      "endTime": "13:30:00",
      "skillsRequirements": [
        "Guard_Moyens"
      ]
    },
    {
      "id": "AFTERNOON_MOYENS",
      "category": "GUARD",
      "description": "Afternoon guard moyens",
      "startTime": "13:30:00",
      "endTime": "18:30:00",
      "skillsRequirements": [
        "Guard_Moyens"
      ]
    },
    {
      "id": "MORNING_GRANDS",
      "category": "GUARD",
      "description": "Morning guard grands",
      "startTime": "09:30:00",
      "endTime": "13:30:00",
      "skillsRequirements": [
        "Guard_Grands"
      ]
    },
    {
      "id": "AFTERNOON_GRANDS",
      "category": "GUARD",
      "description": "Afternoon guard grands",
      "startTime": "13:30:00",
      "endTime": "18:30:00",
      "skillsRequirements": [
        "Guard_Grands"
      ]
    },
    {
      "id": "GYM_MOYENS",
      "category": "GUARD",
      "description": "Gym Moyens",
      "startTime": "09:30:00",
      "endTime": "11:30:00",
      "skillsRequirements": [
        "Gym_Moyens"
      ]
    },
    {
      "id": "GYM_GRANDS",
      "category": "GUARD",
      "description": "Gym Grands",
      "startTime": "09:30:00",
      "endTime": "11:30:00",
      "skillsRequirements": [
        "Gym_Grands"
      ]
    }
  ],
  "coverRequirements": [
    {
      "year": 2020,
      "month": 1,
      "weekDefinitions": [
        {
          "id": "gym_moyen",
          "days": [
            {
              "day": 2,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            },
            {
              "day": 3,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            },
            {
              "day": 4,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            },
            {
              "day": 5,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "GYM_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            },
            {
              "day": 6,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            }
          ]
        },
        {
          "id": "gym_grands",
          "days": [
            {
              "day": 2,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            },
            {
              "day": 3,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            },
            {
              "day": 4,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            },
            {
              "day": 5,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "GYM_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            },
            {
              "day": 6,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  "shiftAbsences": [
    {
      "shiftDefinitionId": "OPEN",
      "familyId": "EMMA",
      "date": "2020-01-11"
    }
  ],
  "dayAbsences": [
    {
      "familyId": "MARCEAU",
      "date": "2020-01-11"
    }
  ]
}
```

### In detail

#### Skills
Families have skills and shifts need families with skills. Declare the competencies required in the system. *Note - I could probably remove this ad collate this list from shift definitions
```json
{
  "skills": [
    "Guard_Moyens",
    "Gym_Moyens",
    "Organise",
    "Guard_Grands",
    "Gym_Grands"
  ]
} 
```

#### Families
Each resource (Family) provides a set of skills and is associated with a contract which provides constraints (as opposed to obligations) on the Family. 
```json
{
  "families": [
    {
      "id": "EMMA",
      "contractId": 0,
      "name": "EMMA",
      "skills": [
        "Organise",
        "Guard_Moyens",
        "Gym_Moyens"
      ]
    },
    {
      "id": "LAUTARO",
      "contractId": 1,
      "name": "LAUTARO",
      "skills": [
        "Organise",
        "Guard_Grands",
        "Gym_Grands"
      ]
    },
    {
      ...
    },
    {
      ...
    }
  ]
}
```

#### Contracts
A family is not obliged to fulfill a contract, rather the contract protects the worker and prevents the family from being given too many shifts.
```json
{
  "contracts": [
    {
      "id": 0,
      "description": "standard_moyens",
      "globalLimits": {
        "daily": 1,
        "weekly": 3
      },
      "shiftRules": [
        {
          "shiftDefinitionIds": [
            "OPEN",
            "CLOSE"
          ],
          "limits": {
            "daily": 1,
            "weekly": 1
          }
        },
        {
          "shiftDefinitionIds": [
            "MORNING_MOYENS",
            "AFTERNOON_MOYENS",
            "GYM_MOYENS"
          ],
          "limits": {
            "daily": 1,
            "weekly": 2
          }
        }
      ]
    },
    {
      "id": 1,
      "description": "standard_grands",
      "globalLimits": {
        "daily": 1
      },
      "shiftRules": [
        {
          "shiftDefinitionIds": [
            "OPEN",
            "CLOSE"
          ],
          "limits": {
            "daily": 1,
            "weekly": 1
          }
        },
        {
          "shiftDefinitionIds": [
            "MORNING_GRANDS",
            "AFTERNOON_GRANDS",
            "GYM_GRANDS"
          ],
          "limits": {
            "daily": 1,
            "weekly": 2
          }
        }
      ]
    }
  ]
}
```

#### Shifts
A shift has an id, start and end times etc. It is important to note the `skillsRequirements`. This is the link between a shift and resource (Family). A resource is only assigned to a shift if it has all the necessary competences (skills).
```json
{
  "shifts": [
    {
      "id": "OPEN",
      "category": "ORGANISE",
      "description": "Open crèche",
      "startTime": "08:00:00",
      "endTime": "09:30:00",
      "skillsRequirements": [
        "Organise"
      ]
    },
    {
      "id": "MORNING_MOYENS",
      "category": "GUARD",
      "description": "Morning guard moyens",
      "startTime": "09:30:00",
      "endTime": "13:30:00",
      "skillsRequirements": [
        "Guard_Moyens"
      ]
    },
    {
      ...
    },
    {
      ...
    }
  ]
}
```

#### Cover
`coverRequirements` defines the resource requirements. You can declare requirements on any given day of a by giving a shift definition id and the number of resources needed.

The month is defined by month of year and then a list of week definitions. The compute will keep looping through the week definitions to generate requirements for each day. That is to say, if a month has 4 weeks and you only define 2 weeks, the 2 weeks will be repeated. See below:
```json
{
  "coverRequirements": [
    {
      "year": 2020,
      "month": 1,
      "weekDefinitions": [
        {
          "id": "gym_moyen",
          "days": [
            {
              "day": 2,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            },
            {
              "day": 3,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            },
            {
              "day": 4,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            },
            {
              "day": 5,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "GYM_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            },
            {
              "day": 6,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "MORNING_MOYENS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            }
          ]
        },
        {
          "id": "gym_grands",
          "days": [
            {
              "day": 2,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            },
            {
              "day": 3,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            },
            {
              "day": 4,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            },
            {
              "day": 5,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            },
            {
              "day": 6,
              "shifts": [
                {
                  "shiftDefinitionId": "OPEN",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "AFTERNOON_GRANDS",
                  "cover": 1
                },
                {
                  "shiftDefinitionId": "CLOSE",
                  "cover": 1
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

#### Absences
You can declare absences per family either by shifts of by entire days
```json
{
  "shiftAbsences": [
    {
      "shiftDefinitionId": "OPEN",
      "familyId": "EMMA",
      "date": "2020-01-11"
    }
  ],
  "dayAbsences": [
    {
      "familyId": "MARCEAU",
      "date": "2020-01-11"
    }
  ]
}
``` 

### TODO
* Complete model validation
* Finish readme and docs
* Remove remaining Play example project code
* Docker
* Maybe implement standard API features such as cache, security etc
* Monthly limits
* Write graphql layer