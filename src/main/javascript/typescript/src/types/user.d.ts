/**
 * For creating a new user (all fields required; checked programmatically) or patching an existing user (any field
 * nullable)
 */
export class User {
    username?: String | null;
    password?: String | null;
    authorizedUntil?: Date | null;
    enabled?: boolean | null;
    admin?: boolean | null;
}