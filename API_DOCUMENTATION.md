# Hostel Management System - Complete API Documentation

## Table of Contents
1. [System Overview](#system-overview)
2. [Authentication & Authorization](#authentication--authorization)
3. [User Roles & Permissions](#user-roles--permissions)
4. [API Endpoints](#api-endpoints)
5. [Data Models](#data-models)
6. [Frontend Requirements](#frontend-requirements)
7. [User Workflows](#user-workflows)
8. [Testing Guide](#testing-guide)

---

## System Overview

The Hostel Management System is a comprehensive backend application built with Spring Boot that manages student registrations, attendance tracking, billing, and administrative functions for a hostel facility.

### Key Features:
- **Student Registration**: Form-based registration with admin approval
- **Attendance Management**: Daily attendance with absence request system
- **Monitor System**: Student monitors can approve early absence requests
- **Billing System**: Monthly expense distribution based on attendance
- **Payment Management**: Balance tracking and payment recording
- **User Management**: Role-based access control (Admin, Warden, Student)

### Technology Stack:
- **Backend**: Spring Boot 3.2.0, Java 21
- **Database**: MySQL 8.0
- **Authentication**: JWT tokens
- **File Storage**: Local file system
- **Build Tool**: Gradle

---

## Authentication & Authorization

### JWT Token System
- **Token Expiry**: 30 minutes (1800000 ms)
- **Token Format**: Bearer token in Authorization header
- **Token Claims**: email, role, isMonitor

### Authentication Flow:
1. User sends login credentials to `/api/auth/login`
2. System validates credentials and returns JWT token
3. Client includes token in Authorization header for subsequent requests
4. Token contains user role and monitor status for authorization

### Authorization Headers:
\`\`\`
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBob3N0ZWwuY29tIiwicm9sZSI6IkFETUlOIiwiaXNNb25pdG9yIjpmYWxzZSwiaWF0IjoxNzA1MzE0NjAwLCJleHAiOjE3MDUzMTY0MDB9.signature
\`\`\`

---

## User Roles & Permissions

### 1. ADMIN
**Capabilities:**
- Add form numbers for student registration
- Approve/reject student registration requests
- Assign/revoke monitor roles
- Enter monthly expenses (triggers bill generation)
- Record payments and manage student balances
- Request student deletion
- View all system data

**Restrictions:**
- Cannot approve late absence requests (Warden only)

### 2. WARDEN
**Capabilities:**
- Approve/reject late absence requests (after 11 AM)
- Approve/reject student deletion requests
- View monthly expenses and financial reports
- View all absence requests

**Restrictions:**
- Cannot manage registrations or payments
- Cannot assign monitor roles

### 3. STUDENT
**Capabilities:**
- Submit registration requests
- Submit absence requests
- View personal attendance history
- View personal dashboard (balance, bills, attendance)

**Restrictions:**
- Cannot access other students' data
- Cannot perform administrative functions

### 4. MONITOR (Student with special privileges)
**Capabilities:**
- All student capabilities
- Approve/reject early absence requests (before 11 AM)
- View early absence requests from other students

**Restrictions:**
- Cannot approve late absence requests
- Cannot access administrative functions

---

## API Endpoints

### Base URL
\`\`\`
http://localhost:8080
\`\`\`

### 1. Authentication Endpoints

#### POST /api/auth/login
**Description:** User authentication
**Access:** Public
**Content-Type:** application/json

**Request:**
\`\`\`json
{
"email": "admin@hostel.com",
"password": "admin123"
}
\`\`\`

**Response:**
\`\`\`json
{
"success": true,
"message": "Login successful",
"data": {
"token": "eyJhbGciOiJIUzI1NiJ9...",
"email": "admin@hostel.com",
"role": "ADMIN",
"isMonitor": false,
"fullName": null
},
"timestamp": "2024-01-15T10:30:00"
}
\`\`\`

---

### 2. Student Endpoints

#### POST /api/students/register
**Description:** Submit student registration request
**Access:** Public
**Content-Type:** multipart/form-data

**Form Data:**
\`\`\`
formNumber: HM2024001
email: student1@example.com
password: password123
enrollmentNo: EN2024001
fullName: John Doe
phone: 9876543210
department: Computer Science
batch: 2024
pincode: 123456
district: Mumbai
tehsil: Andheri
guardianPhone: 9876543211
photo: [File - JPEG/PNG, max 2MB]
\`\`\`

**Response:**
\`\`\`json
{
"success": true,
"message": "Registration request submitted successfully",
"data": null,
"timestamp": "2024-01-15T10:30:00"
}
\`\`\`

#### POST /api/students/absence-request
**Description:** Submit absence request
**Access:** Student only
**Content-Type:** application/json

**Request:**
\`\`\`json
{
"absenceDate": "2024-01-20",
"reason": "Medical appointment with family doctor for regular checkup"
}
\`\`\`

**Response:**
\`\`\`json
{
"success": true,
"message": "Absence request submitted successfully",
"data": null,
"timestamp": "2024-01-15T10:30:00"
}
\`\`\`

#### GET /api/students/attendance-history
**Description:** Get student's attendance history
**Access:** Student only
**Query Parameters:**
- `months` (optional, default: 3) - Number of months to retrieve

**Response:**
\`\`\`json
{
"success": true,
"data": [
{
"attendanceId": 1,
"date": "2024-01-15",
"status": "PRESENT",
"approvedBy": null,
"approvedAt": null,
"createdAt": "2024-01-15T00:01:00"
}
],
"timestamp": "2024-01-15T10:30:00"
}
\`\`\`

#### GET /api/students/dashboard
**Description:** Get student dashboard data
**Access:** Student only

**Response:**
\`\`\`json
{
"success": true,
"data": {
"currentBalance": 5000.00,
"pendingBillAmount": 2000.00,
"netBalance": 3000.00,
"monthlyExpenses": 8000.00,
"presentDaysThisMonth": 25,
"totalDaysThisMonth": 31,
"isMonitor": false,
"fullName": "John Doe"
},
"timestamp": "2024-01-15T10:30:00"
}
\`\`\`

---

### 3. Admin Endpoints

#### POST /api/admin/form-numbers
**Description:** Add form numbers for student registration
**Access:** Admin only
**Content-Type:** application/json

**Request:**
\`\`\`json
[
"HM2024006",
"HM2024007",
"HM2024008"
]
\`\`\`

#### GET /api/admin/registration-requests
**Description:** Get pending registration requests
**Access:** Admin only

**Response:**
\`\`\`json
{
"success": true,
"data": [
{
"requestId": 1,
"formNumber": {
"formNumber": "HM2024001"
},
"email": "student1@example.com",
"enrollmentNo": "EN2024001",
"fullName": "John Doe",
"phone": "9876543210",
"department": "Computer Science",
"batch": "2024",
"pincode": "123456",
"district": "Mumbai",
"tehsil": "Andheri",
"guardianPhone": "9876543211",
"photoUrl": "uuid-filename.jpg",
"status": "PENDING",
"createdAt": "2024-01-15T10:30:00"
}
],
"timestamp": "2024-01-15T10:30:00"
}
\`\`\`

#### PUT /api/admin/registration-requests/{id}/approve
**Description:** Approve registration request
**Access:** Admin only

#### PUT /api/admin/registration-requests/{id}/reject
**Description:** Reject registration request
**Access:** Admin only
**Query Parameters:**
- `comments` (required) - Reason for rejection

#### POST /api/admin/monitor/{studentId}
**Description:** Assign monitor role to student
**Access:** Admin only

#### POST /api/admin/expenses
**Description:** Enter monthly expense and generate bills
**Access:** Admin only
**Query Parameters:**
- `monthYear` (required) - Format: YYYY-MM
- `totalAmount` (required) - Total monthly expense amount

#### POST /api/admin/payments
**Description:** Record payment and add to student balance
**Access:** Admin only
**Content-Type:** application/json

**Request:**
\`\`\`json
{
"studentId": 1,
"amount": 5000.00,
"method": "CASH",
"transactionId": "TXN123456"
}
\`\`\`

#### POST /api/admin/deletion-request/{studentId}
**Description:** Request student deletion
**Access:** Admin only
**Query Parameters:**
- `reason` (required) - Reason for deletion

---

### 4. Warden Endpoints

#### GET /api/warden/deletion-requests
**Description:** Get pending deletion requests
**Access:** Warden only

**Response:**
\`\`\`json
{
"success": true,
"data": [
{
"requestId": 1,
"student": {
"studentId": 1,
"fullName": "John Doe",
"enrollmentNo": "EN2024001"
},
"requestedBy": {
"email": "admin@hostel.com"
},
"reason": "Student graduated",
"status": "PENDING",
"createdAt": "2024-01-15T10:30:00"
}
],
"timestamp": "2024-01-15T10:30:00"
}
\`\`\`

#### PUT /api/warden/deletion-requests/{id}/approve
**Description:** Approve deletion request
**Access:** Warden only

#### PUT /api/warden/deletion-requests/{id}/reject
**Description:** Reject deletion request
**Access:** Warden only
**Query Parameters:**
- `reason` (required) - Reason for rejection

#### GET /api/warden/expenses
**Description:** Get all monthly expenses
**Access:** Warden only

#### GET /api/warden/expenses/{monthYear}
**Description:** Get specific monthly expense
**Access:** Warden only

#### GET /api/warden/absence-requests/late
**Description:** Get late absence requests (after 11 AM)
**Access:** Warden only

#### PUT /api/warden/absence-requests/{id}/approve
**Description:** Approve absence request
**Access:** Warden only
**Query Parameters:**
- `comments` (optional) - Approval comments

#### PUT /api/warden/absence-requests/{id}/reject
**Description:** Reject absence request
**Access:** Warden only
**Query Parameters:**
- `reason` (required) - Rejection reason

---

### 5. Monitor Endpoints

#### GET /api/monitor/absence-requests/early
**Description:** Get early absence requests (before 11 AM)
**Access:** Monitor only

**Response:**
\`\`\`json
{
"success": true,
"data": [
{
"requestId": 1,
"student": {
"fullName": "Jane Smith",
"enrollmentNo": "EN2024002"
},
"requestDate": "2024-01-15",
"absenceDate": "2024-01-16",
"reason": "Medical appointment",
"status": "PENDING",
"isLateRequest": false,
"submittedAt": "2024-01-15T09:30:00"
}
],
"timestamp": "2024-01-15T10:30:00"
}
\`\`\`

#### PUT /api/monitor/absence-requests/{id}/approve
**Description:** Approve absence request
**Access:** Monitor only
**Query Parameters:**
- `comments` (optional) - Approval comments

#### PUT /api/monitor/absence-requests/{id}/reject
**Description:** Reject absence request
**Access:** Monitor only
**Query Parameters:**
- `reason` (required) - Rejection reason

---

## Data Models

### User
\`\`\`json
{
"userId": 1,
"email": "user@example.com",
"role": "ADMIN|WARDEN|STUDENT",
"createdAt": "2024-01-15T10:30:00",
"updatedAt": "2024-01-15T10:30:00"
}
\`\`\`

### Student
\`\`\`json
{
"studentId": 1,
"user": {
"userId": 1,
"email": "student@example.com",
"role": "STUDENT"
},
"enrollmentNo": "EN2024001",
"fullName": "John Doe",
"phone": "9876543210",
"department": "Computer Science",
"batch": "2024",
"pincode": "123456",
"district": "Mumbai",
"tehsil": "Andheri",
"guardianPhone": "9876543211",
"photoUrl": "uuid-filename.jpg",
"isMonitor": false,
"currentBalance": 5000.00,
"createdAt": "2024-01-15T10:30:00",
"updatedAt": "2024-01-15T10:30:00"
}
\`\`\`

### Attendance
\`\`\`json
{
"attendanceId": 1,
"student": {
"studentId": 1,
"fullName": "John Doe"
},
"date": "2024-01-15",
"status": "PRESENT|ABSENT",
"approvedBy": {
"userId": 2,
"email": "monitor@example.com"
},
"approvedAt": "2024-01-15T11:00:00",
"createdAt": "2024-01-15T00:01:00"
}
\`\`\`

### Bill
\`\`\`json
{
"billId": 1,
"student": {
"studentId": 1,
"fullName": "John Doe"
},
"monthYear": "2024-01",
"amountDue": 2500.00,
"amountPaid": 1000.00,
"presentDays": 25,
"totalDays": 31,
"status": "PENDING|PARTIALLY_PAID|FULLY_PAID",
"createdAt": "2024-01-15T10:30:00",
"updatedAt": "2024-01-15T10:30:00"
}
\`\`\`

### AbsenceRequest
\`\`\`json
{
"requestId": 1,
"student": {
"studentId": 1,
"fullName": "John Doe",
"enrollmentNo": "EN2024001"
},
"requestDate": "2024-01-15",
"absenceDate": "2024-01-16",
"reason": "Medical appointment",
"status": "PENDING|APPROVED|REJECTED",
"approvedBy": {
"userId": 2,
"email": "monitor@example.com"
},
"comments": "Approved for medical reasons",
"submittedAt": "2024-01-15T09:30:00",
"approvedAt": "2024-01-15T11:00:00",
"isLateRequest": false,
"createdAt": "2024-01-15T09:30:00"
}
\`\`\`

---

## Frontend Requirements

### 1. Technology Recommendations
- **Framework**: React.js with TypeScript or Vue.js
- **State Management**: Redux Toolkit or Zustand
- **HTTP Client**: Axios with interceptors for token management
- **UI Library**: Material-UI, Ant Design, or Tailwind CSS
- **Form Handling**: React Hook Form or Formik
- **File Upload**: React Dropzone or similar
- **Date Handling**: date-fns or dayjs
- **Routing**: React Router or Vue Router

### 2. Required Pages/Components

#### Authentication
- **Login Page**: Email/password form with role-based redirection
- **Protected Route Component**: HOC for route protection

#### Admin Dashboard
- **Dashboard Overview**: System statistics and quick actions
- **Form Numbers Management**: Add/view form numbers
- **Registration Requests**: List, approve/reject requests
- **Student Management**: List students, assign monitor roles
- **Expense Management**: Enter monthly expenses
- **Payment Management**: Record payments, view balances
- **Deletion Requests**: Submit deletion requests

#### Warden Dashboard
- **Dashboard Overview**: Warden-specific statistics
- **Deletion Requests**: Approve/reject deletion requests
- **Expense Reports**: View monthly expenses and reports
- **Late Absence Requests**: Handle requests after 11 AM

#### Student Dashboard
- **Personal Dashboard**: Balance, bills, attendance summary
- **Registration Form**: Multi-step form with file upload
- **Absence Request Form**: Submit absence requests
- **Attendance History**: Calendar view of attendance
- **Bill History**: View bills and payment history

#### Monitor Dashboard (Student + Monitor features)
- **All Student Features**: Plus monitor-specific features
- **Early Absence Requests**: Handle requests before 11 AM

### 3. Key UI Components

#### Common Components
\`\`\`typescript
// API Response wrapper
interface ApiResponse<T> {
success: boolean;
message?: string;
data?: T;
timestamp: string;
}

// Authentication context
interface AuthContext {
user: User | null;
token: string | null;
login: (credentials: LoginRequest) => Promise<void>;
logout: () => void;
isAuthenticated: boolean;
hasRole: (role: string) => boolean;
isMonitor: boolean;
}

// Protected Route component
interface ProtectedRouteProps {
children: React.ReactNode;
requiredRole?: string;
requireMonitor?: boolean;
}
\`\`\`

#### Form Components
- **StudentRegistrationForm**: Multi-step form with validation
- **AbsenceRequestForm**: Date picker and text area
- **PaymentForm**: Amount, method, transaction ID
- **ExpenseForm**: Month/year picker and amount

#### List Components
- **RegistrationRequestsList**: Filterable table with actions
- **AbsenceRequestsList**: Different views for monitor/warden
- **StudentsList**: Searchable list with actions
- **AttendanceHistory**: Calendar or table view

#### Dashboard Components
- **StatisticsCards**: Key metrics display
- **RecentActivity**: Timeline of recent actions
- **QuickActions**: Buttons for common tasks
- **BalanceDisplay**: Current balance and net balance

### 4. State Management Structure

\`\`\`typescript
// Auth State
interface AuthState {
user: User | null;
token: string | null;
isLoading: boolean;
error: string | null;
}

// Student State
interface StudentState {
profile: Student | null;
dashboard: StudentDashboardResponse | null;
attendanceHistory: Attendance[];
isLoading: boolean;
error: string | null;
}

// Admin State
interface AdminState {
registrationRequests: RegistrationRequest[];
students: Student[];
formNumbers: string[];
isLoading: boolean;
error: string | null;
}

// Absence Requests State
interface AbsenceRequestsState {
earlyRequests: AbsenceRequest[];
lateRequests: AbsenceRequest[];
myRequests: AbsenceRequest[];
isLoading: boolean;
error: string | null;
}
\`\`\`

### 5. HTTP Client Setup

\`\`\`typescript
// Axios configuration
const apiClient = axios.create({
baseURL: 'http://localhost:8080',
timeout: 10000,
});

// Request interceptor for token
apiClient.interceptors.request.use((config) => {
const token = localStorage.getItem('token');
if (token) {
config.headers.Authorization = `Bearer ${token}`;
}
return config;
});

// Response interceptor for error handling
apiClient.interceptors.response.use(
(response) => response,
(error) => {
if (error.response?.status === 401) {
// Redirect to login
localStorage.removeItem('token');
window.location.href = '/login';
}
return Promise.reject(error);
}
);
\`\`\`

---

## User Workflows

### 1. Student Registration Workflow
1. **Student**: Fills registration form with photo upload
2. **System**: Validates form number and creates registration request
3. **Admin**: Reviews registration request
4. **Admin**: Approves/rejects request
5. **System**: Creates student account if approved
6. **Student**: Can login with default password

### 2. Attendance & Absence Workflow
1. **System**: Creates daily attendance records (12:01 AM)
2. **Student**: Submits absence request
3. **Monitor**: Approves early requests (before 11 AM)
4. **Warden**: Approves late requests (after 11 AM)
5. **System**: Updates attendance record

### 3. Billing Workflow
1. **Admin**: Enters monthly expense
2. **System**: Generates bills based on attendance
3. **Admin**: Records payments
4. **System**: Updates student balance
5. **Student**: Views balance and bills on dashboard

### 4. Monitor Assignment Workflow
1. **Admin**: Assigns monitor role to student
2. **System**: Updates student record
3. **Monitor**: Gets access to early absence requests
4. **Monitor**: Can approve/reject early requests

### 5. Student Deletion Workflow
1. **Admin**: Submits deletion request
2. **Warden**: Reviews deletion request
3. **Warden**: Approves/rejects request
4. **System**: Deletes student data if approved

---

## Testing Guide

### Default Credentials
\`\`\`
Admin:
- Email: admin@hostel.com
- Password: admin123

Warden:
- Email: warden@hostel.com
- Password: admin123

Students:
- After registration approval
- Default password: defaultPassword123
  \`\`\`

### Testing Sequence
1. **Setup**: Login as admin, add form numbers
2. **Registration**: Register student, approve as admin
3. **Monitor**: Assign monitor role to student
4. **Absence**: Submit and approve absence requests
5. **Billing**: Enter expenses, record payments
6. **Deletion**: Request and approve student deletion

### Error Handling
- **401 Unauthorized**: Token expired or invalid
- **403 Forbidden**: Insufficient permissions
- **400 Bad Request**: Validation errors
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error

---

## Security Considerations

### Frontend Security
1. **Token Storage**: Use secure storage (httpOnly cookies preferred)
2. **Route Protection**: Implement proper route guards
3. **Input Validation**: Client-side validation for UX
4. **File Upload**: Validate file types and sizes
5. **Error Handling**: Don't expose sensitive information

### API Security
1. **CORS**: Configure for production domains
2. **Rate Limiting**: Implement on sensitive endpoints
3. **Input Sanitization**: Server handles validation
4. **File Security**: Server validates uploads
5. **JWT Security**: Tokens expire in 30 minutes

---

This documentation provides everything needed to build a complete frontend application for the Hostel Management System. It includes all API endpoints, data models, user workflows, and specific frontend requirements with code examples.
