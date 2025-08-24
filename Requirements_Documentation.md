# Hostel Management System - Updated Requirements Document (v2.1)

## Project Overview
Create a comprehensive hostel management web application with attendance tracking as the primary module. The system should be scalable to accommodate additional features in future iterations.

## Technology Stack
- **Backend**: Java Spring Boot
- **Frontend**: React.js
- **Database**: MySQL
- **Authentication**: JWT-based authentication
- **File Storage**: Local storage or cloud storage for student photos

## User Roles and Permissions

### 1. ADMIN
**Primary Responsibilities:**
- Populate and manage student form numbers in database
- Verify and approve student registration requests
- Designate and manage monitor roles
- Enter monthly total expenses
- View current balance and net balance of all students
- Process student payments and update balances
- Send student deletion requests to WARDEN
- Password reset functionality

### 2. WARDEN
**Primary Responsibilities:**
- Approve student deletion requests from ADMIN (permanent deletion)
- Approve absence requests submitted after 11 AM
- View monthly total expenses
- View current balance and net balance of all students
- Password reset functionality

### 3. STUDENT
**Primary Responsibilities:**
- Self-registration using form number
- Login and access personal dashboard
- Submit absence requests, default present
- View attendance history (last 3 months)
- View current balance, net balance, pending bills and monthly expenses
- Password reset functionality

### 4. MONITOR (Special Student Role)
**Additional Responsibilities:**
- Approve absence requests submitted before 11 AM
- Only one monitor can exist at a time (enforced by backend logic)
- Role can be transferred by ADMIN (backend atomically removes previous monitor)
- Monitor is tracked via `is_monitor` field in `students` table

## Detailed Functional Requirements

### Authentication & User Management

#### Student Registration Process
1. **Form Number Verification**
    - Student enters form number on registration page
    - System validates form number exists in pre-populated database
    - If invalid, display error message and prevent registration
    - If valid, proceed to registration form

2. **Registration Form Fields**
    - Email address (will be used for login)
    - Password (to be set by student)
    - Full university enrollment number
    - Full name
    - Phone number
    - Department
    - Batch/Year
    - Pin code
    - District
    - Tehsil
    - Guardian phone number
    - Student photograph upload

3. **Registration Verification of Students**
    - All registration requests require ADMIN approval
    - ADMIN can approve or reject requests
    - Only approved students can login to the system

#### Login System
- Students login using email and password
- JWT token-based authentication
- Session management and security
- Password reset functionality for all user types

### Attendance Management System

#### Default Attendance Status
- All students are marked present by default each day
- Students must actively request absence if needed

#### Absence Request Process
1. **Before 11:00 AM**
    - Students can submit absence requests
    - MONITOR approves/rejects these requests
    - Immediate processing required

2. **After 11:00 AM**
    - Students can still submit absence requests
    - Only WARDEN can approve these requests

#### Attendance History
- Students can view their attendance for the last 3 months
- Display in calendar or list format
- Show present/absent status for each day
- Include reasons for absence if provided

### Financial Management

#### Expense Calculation System
1. **Monthly Expense Entry**
    - ADMIN enters total monthly expenses for the hostel
    - System calculates per-day expense rate
    - Individual bills calculated based on days present

2. **Bill Generation Formula**
   ```
   Student Monthly Bill = (Total Monthly Expenses / Sum of total number of days students are present in the Month) × Days Present of that student
   ```

3. **Payment Processing**
    - ADMIN records payments received and updates student balances
    - Support for partial, full, and advance payments
    - Maintain payment history and outstanding balance/advance balance
    - Generate payment receipts

4. **Balance Management**
    - Each student has a `current_balance` field tracking available funds
    - System calculates `netBalance` = current_balance - pending dues
    - `netBalance` indicates:
        - Positive value: Student has advance payment
        - Negative value: Student owes money
    - Balance information displayed on all relevant dashboards
    - **Net balance is calculated on-the-fly** and accessed through the student dashboard endpoint

#### Financial Dashboard
- Students view:
    - Current balance
    - Net balance (current balance - pending dues)
    - Pending bills
    - Monthly expenses
- ADMIN and WARDEN view:
    - Overall financial reports
    - Student balance summaries
    - Payment tracking and history

### Administrative Functions

#### Student Management
1. **Form Number Population**
    - ADMIN populates database with valid form numbers
    - Bulk import functionality
    - Edit/delete form numbers as needed

2. **Monitor Management**
    - ADMIN designates one student as monitor
    - When new monitor is set, previous monitor role is automatically removed
    - Only one monitor can exist at any time
    - Monitor retains all student privileges plus approval rights

3. **Student Deletion Process**
    - ADMIN initiates student deletion request
    - WARDEN must approve deletion requests
    - **Permanent deletion**: Student and all related data removed from database
    - Backend handles cascading deletion of related records


## UI/UX Requirements

### Design Principles
- **Responsive Design**: Mobile-first approach
- **Clean Interface**: Minimalist design with clear navigation
- **Accessibility**: WCAG 2.1 AA compliance
- **Performance**: Fast loading times and smooth interactions
- **Security**: Secure form handling and data protection

### Dashboard Requirements

#### Student Dashboard
- **Current balance** display (stored in database and managed by admins)
- **Net balance** indicator (calculated on-the-fly: current_balance - pending_dues)
- Pending bill amount with due dates
- Quick absence request button
- Attendance summary for current month
- Recent payment history
- Profile information access

#### Monitor Dashboard
- All student dashboard features
- Pending absence requests (before cut-off time only)
- Quick approve/reject buttons
- Monitor-specific notifications
- Monitor badge/indicator

#### ADMIN Dashboard
- Pending registration requests
- Financial overview (monthly expenses, collections)
- Student management tools
- System statistics and reports
- Student balance overview (current and net balances)
- Monitor management section

#### WARDEN Dashboard
- Financial overview
- Pending deletion requests
- Late absence requests (after 11 AM)
- Overall hostel statistics
- Student balance overview (current and net balances)

## Security Requirements

### Data Protection
- Password hashing using BCrypt
- JWT token expiration and refresh
- Input validation and sanitization
- SQL injection prevention
- XSS protection
- CSRF protection

### File Upload Security
- File type validation for student photos
- File size limits
- Secure file storage
- Image processing and optimization

## Performance Requirements

### Response Time
- Page load time: < 2 seconds
- API response time: < 500ms
- Database query optimization
- Efficient pagination for large datasets

### Scalability
- Support for 500+ concurrent users
- Database indexing strategy
- Caching implementation
- Load balancing considerations

## Testing Requirements

### Unit Testing
- Service layer testing
- Repository layer testing
- Utility function testing
- Minimum 80% code coverage
- Balance calculation logic tests

### Integration Testing
- API endpoint testing
- Database integration testing
- Authentication flow testing
- File upload testing
- Payment processing workflows

### User Acceptance Testing
- Role-based access testing
- End-to-end workflow testing
- Cross-browser compatibility
- Mobile responsiveness testing
- Balance display validation

## Deployment Requirements

### Environment Setup
- Development, staging, and production environments
- Environment-specific configuration
- Database migration scripts
- Automated deployment pipeline

### Monitoring and Logging
- Application logging
- Error tracking
- Performance monitoring
- User activity logging
- Balance change audit trails

## Future Enhancement Considerations

### Planned Features
- Fee management system
- Room allocation system
- Complaint management
- Visitor management
- Mess menu and feedback
- Mobile application
- Notification system (email/SMS)
- Report generation and analytics

### Technical Improvements
- Redis caching for balance calculations
- Email/SMS notifications for balance changes
- Cloud storage integration
- Advanced financial reporting dashboard
- API rate limiting
- Advanced security features
- Balance forecasting system
- Dedicated net balance endpoint (if needed in future)



## Success Metrics

### Functional Metrics
- 100% accurate balance calculations
- < 5 minute balance update latency
- 99.9% payment processing success rate
- 100% accurate net balance displays

### Performance Metrics
- Balance calculation time < 100ms
- Dashboard load time < 1.5 seconds
- 99.9% system uptime
- User satisfaction score > 4.7/5 for financial features

### Security Metrics
- Zero financial data breaches
- 100% secure payment processing
- Regular security audits passed
- No unauthorized balance modifications

