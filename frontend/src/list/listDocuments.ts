import { graphql } from '../gql'

/**
 * Allt Min lista behöver. Varje ändring returnerar hela fragmentet, så att Apollo-cachen
 * (och därmed totalpoäng och rangordning) uppdateras direkt efter svaret.
 */
graphql(`
  fragment MyListWorkspace on Workspace {
    id
    access
    meritValue
    weights {
      criterion
      weight
    }
    watchedSchools {
      status
      note
      totalScore
      missingScores
      scores {
        criterion
        score
      }
      program {
        code
        name
      }
      school {
        code
        ...SchoolCard
      }
    }
  }
`)

export const MyListQuery = graphql(`
  query MyList {
    workspace {
      ...MyListWorkspace
    }
  }
`)

export const SetWeightMutation = graphql(`
  mutation SetWeight($criterion: Criterion!, $weight: Int!) {
    setWeight(criterion: $criterion, weight: $weight) {
      ...MyListWorkspace
    }
  }
`)

export const SetScoreMutation = graphql(`
  mutation SetScore($school: ID!, $criterion: Criterion!, $score: Int) {
    setScore(school: $school, criterion: $criterion, score: $score) {
      ...MyListWorkspace
    }
  }
`)

export const SetStatusMutation = graphql(`
  mutation SetStatus($school: ID!, $status: WatchStatus) {
    setStatus(school: $school, status: $status) {
      ...MyListWorkspace
    }
  }
`)

export const SetNoteMutation = graphql(`
  mutation SetNote($school: ID!, $note: String) {
    setNote(school: $school, note: $note) {
      ...MyListWorkspace
    }
  }
`)
