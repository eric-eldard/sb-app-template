// Shim to attach module namespaces to global context
import { App } from './app';
import { UserManagement } from './user-management';

(window as any).App = App;
(window as any).UserManagement = UserManagement;