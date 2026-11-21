/* eslint-disable */
import * as types from './graphql';
import type { TypedDocumentNode as DocumentNode } from '@graphql-typed-document-node/core';

/**
 * Map of all GraphQL operations in the project.
 *
 * This map has several performance disadvantages:
 * 1. It is not tree-shakeable, so it will include all operations in the project.
 * 2. It is not minifiable, so the string of a GraphQL query will be multiple times inside the bundle.
 * 3. It does not support dead code elimination, so it will add unused operations.
 *
 * Therefore it is highly recommended to use the babel or swc plugin for production.
 * Learn more about it here: https://the-guild.dev/graphql/codegen/plugins/presets/preset-client#reducing-bundle-size
 */
type Documents = {
    "\n  query MyList {\n    workspace {\n      id\n      watchedSchools {\n        program {\n          code\n          name\n        }\n        school {\n          code\n          ...SchoolCard\n        }\n      }\n    }\n  }\n": typeof types.MyListDocument,
    "\n  fragment SchoolCard on School {\n    code\n    name\n    city\n    organizerType\n    webUrl\n    municipality {\n      code # nyckelfält för Apollos cache (keyFields i apollo.ts), måste alltid vara med\n      name\n    }\n    programs {\n      program {\n        code\n      }\n      admissionPointsMin\n      admissionYear\n      admissionNote\n      studyPaths {\n        code\n        name\n      }\n    }\n  }\n": typeof types.SchoolCardFragmentDoc,
    "\n  query SearchOptions {\n    programs {\n      code\n      name\n      category\n    }\n    municipalities {\n      code\n      name\n      county\n    }\n  }\n": typeof types.SearchOptionsDocument,
    "\n  query SearchSchools($program: ID!, $municipalities: [ID!]) {\n    schools(program: $program, municipalities: $municipalities) {\n      code\n      ...SchoolCard\n    }\n  }\n": typeof types.SearchSchoolsDocument,
    "\n  fragment WorkspaceFields on Workspace {\n    id\n    access\n    viewKey\n    watchedSchools {\n      school {\n        code\n      }\n      program {\n        code\n      }\n    }\n  }\n": typeof types.WorkspaceFieldsFragmentDoc,
    "\n  query Workspace {\n    workspace {\n      ...WorkspaceFields\n    }\n  }\n": typeof types.WorkspaceDocument,
    "\n  mutation CreateWorkspace {\n    createWorkspace {\n      editKey\n    }\n  }\n": typeof types.CreateWorkspaceDocument,
    "\n  mutation WatchSchool($school: ID!, $program: ID!) {\n    watchSchool(school: $school, program: $program) {\n      ...WorkspaceFields\n    }\n  }\n": typeof types.WatchSchoolDocument,
    "\n  mutation UnwatchSchool($school: ID!) {\n    unwatchSchool(school: $school) {\n      ...WorkspaceFields\n    }\n  }\n": typeof types.UnwatchSchoolDocument,
};
const documents: Documents = {
    "\n  query MyList {\n    workspace {\n      id\n      watchedSchools {\n        program {\n          code\n          name\n        }\n        school {\n          code\n          ...SchoolCard\n        }\n      }\n    }\n  }\n": types.MyListDocument,
    "\n  fragment SchoolCard on School {\n    code\n    name\n    city\n    organizerType\n    webUrl\n    municipality {\n      code # nyckelfält för Apollos cache (keyFields i apollo.ts), måste alltid vara med\n      name\n    }\n    programs {\n      program {\n        code\n      }\n      admissionPointsMin\n      admissionYear\n      admissionNote\n      studyPaths {\n        code\n        name\n      }\n    }\n  }\n": types.SchoolCardFragmentDoc,
    "\n  query SearchOptions {\n    programs {\n      code\n      name\n      category\n    }\n    municipalities {\n      code\n      name\n      county\n    }\n  }\n": types.SearchOptionsDocument,
    "\n  query SearchSchools($program: ID!, $municipalities: [ID!]) {\n    schools(program: $program, municipalities: $municipalities) {\n      code\n      ...SchoolCard\n    }\n  }\n": types.SearchSchoolsDocument,
    "\n  fragment WorkspaceFields on Workspace {\n    id\n    access\n    viewKey\n    watchedSchools {\n      school {\n        code\n      }\n      program {\n        code\n      }\n    }\n  }\n": types.WorkspaceFieldsFragmentDoc,
    "\n  query Workspace {\n    workspace {\n      ...WorkspaceFields\n    }\n  }\n": types.WorkspaceDocument,
    "\n  mutation CreateWorkspace {\n    createWorkspace {\n      editKey\n    }\n  }\n": types.CreateWorkspaceDocument,
    "\n  mutation WatchSchool($school: ID!, $program: ID!) {\n    watchSchool(school: $school, program: $program) {\n      ...WorkspaceFields\n    }\n  }\n": types.WatchSchoolDocument,
    "\n  mutation UnwatchSchool($school: ID!) {\n    unwatchSchool(school: $school) {\n      ...WorkspaceFields\n    }\n  }\n": types.UnwatchSchoolDocument,
};

/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 *
 *
 * @example
 * ```ts
 * const query = graphql(`query GetUser($id: ID!) { user(id: $id) { name } }`);
 * ```
 *
 * The query argument is unknown!
 * Please regenerate the types.
 */
export function graphql(source: string): unknown;

/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 */
export function graphql(source: "\n  query MyList {\n    workspace {\n      id\n      watchedSchools {\n        program {\n          code\n          name\n        }\n        school {\n          code\n          ...SchoolCard\n        }\n      }\n    }\n  }\n"): (typeof documents)["\n  query MyList {\n    workspace {\n      id\n      watchedSchools {\n        program {\n          code\n          name\n        }\n        school {\n          code\n          ...SchoolCard\n        }\n      }\n    }\n  }\n"];
/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 */
export function graphql(source: "\n  fragment SchoolCard on School {\n    code\n    name\n    city\n    organizerType\n    webUrl\n    municipality {\n      code # nyckelfält för Apollos cache (keyFields i apollo.ts), måste alltid vara med\n      name\n    }\n    programs {\n      program {\n        code\n      }\n      admissionPointsMin\n      admissionYear\n      admissionNote\n      studyPaths {\n        code\n        name\n      }\n    }\n  }\n"): (typeof documents)["\n  fragment SchoolCard on School {\n    code\n    name\n    city\n    organizerType\n    webUrl\n    municipality {\n      code # nyckelfält för Apollos cache (keyFields i apollo.ts), måste alltid vara med\n      name\n    }\n    programs {\n      program {\n        code\n      }\n      admissionPointsMin\n      admissionYear\n      admissionNote\n      studyPaths {\n        code\n        name\n      }\n    }\n  }\n"];
/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 */
export function graphql(source: "\n  query SearchOptions {\n    programs {\n      code\n      name\n      category\n    }\n    municipalities {\n      code\n      name\n      county\n    }\n  }\n"): (typeof documents)["\n  query SearchOptions {\n    programs {\n      code\n      name\n      category\n    }\n    municipalities {\n      code\n      name\n      county\n    }\n  }\n"];
/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 */
export function graphql(source: "\n  query SearchSchools($program: ID!, $municipalities: [ID!]) {\n    schools(program: $program, municipalities: $municipalities) {\n      code\n      ...SchoolCard\n    }\n  }\n"): (typeof documents)["\n  query SearchSchools($program: ID!, $municipalities: [ID!]) {\n    schools(program: $program, municipalities: $municipalities) {\n      code\n      ...SchoolCard\n    }\n  }\n"];
/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 */
export function graphql(source: "\n  fragment WorkspaceFields on Workspace {\n    id\n    access\n    viewKey\n    watchedSchools {\n      school {\n        code\n      }\n      program {\n        code\n      }\n    }\n  }\n"): (typeof documents)["\n  fragment WorkspaceFields on Workspace {\n    id\n    access\n    viewKey\n    watchedSchools {\n      school {\n        code\n      }\n      program {\n        code\n      }\n    }\n  }\n"];
/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 */
export function graphql(source: "\n  query Workspace {\n    workspace {\n      ...WorkspaceFields\n    }\n  }\n"): (typeof documents)["\n  query Workspace {\n    workspace {\n      ...WorkspaceFields\n    }\n  }\n"];
/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 */
export function graphql(source: "\n  mutation CreateWorkspace {\n    createWorkspace {\n      editKey\n    }\n  }\n"): (typeof documents)["\n  mutation CreateWorkspace {\n    createWorkspace {\n      editKey\n    }\n  }\n"];
/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 */
export function graphql(source: "\n  mutation WatchSchool($school: ID!, $program: ID!) {\n    watchSchool(school: $school, program: $program) {\n      ...WorkspaceFields\n    }\n  }\n"): (typeof documents)["\n  mutation WatchSchool($school: ID!, $program: ID!) {\n    watchSchool(school: $school, program: $program) {\n      ...WorkspaceFields\n    }\n  }\n"];
/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 */
export function graphql(source: "\n  mutation UnwatchSchool($school: ID!) {\n    unwatchSchool(school: $school) {\n      ...WorkspaceFields\n    }\n  }\n"): (typeof documents)["\n  mutation UnwatchSchool($school: ID!) {\n    unwatchSchool(school: $school) {\n      ...WorkspaceFields\n    }\n  }\n"];

export function graphql(source: string) {
  return (documents as any)[source] ?? {};
}

export type DocumentType<TDocumentNode extends DocumentNode<any, any>> = TDocumentNode extends DocumentNode<  infer TType,  any>  ? TType  : never;