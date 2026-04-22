package com.grpc.employee;

import com.grpc.employee.generated.Employee;
import com.grpc.employee.generated.EmployeeServiceGrpc;
import com.grpc.employee.generated.GetEmployeeRequest;
import com.grpc.employee.generated.GetEmployeeResponse;
import com.grpc.employee.generated.ListEmployeesRequest;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

public class EmployeeServiceImpl extends EmployeeServiceGrpc.EmployeeServiceImplBase {

    // ─── Dummy Data ──────────────────────────────────────────────────────
    private static final List<Employee> EMPLOYEES = new ArrayList<>();

    static {
        EMPLOYEES.add(build("E001", "Alice Johnson",  "Engineering", "alice@company.com",  95000));
        EMPLOYEES.add(build("E002", "Bob Smith",      "Engineering", "bob@company.com",    88000));
        EMPLOYEES.add(build("E003", "Carol White",    "HR",          "carol@company.com",  72000));
        EMPLOYEES.add(build("E004", "David Brown",    "HR",          "david@company.com",  68000));
        EMPLOYEES.add(build("E005", "Eva Martinez",   "Finance",     "eva@company.com",    91000));
        EMPLOYEES.add(build("E006", "Frank Lee",      "Finance",     "frank@company.com",  87000));
        EMPLOYEES.add(build("E007", "Grace Kim",      "Engineering", "grace@company.com",  99000));
        EMPLOYEES.add(build("E008", "Henry Wilson",   "Marketing",   "henry@company.com",  74000));
    }

    private static Employee build(String id, String name, String dept, String email, double salary) {
        return Employee.newBuilder()
                .setId(id)
                .setName(name)
                .setDepartment(dept)
                .setEmail(email)
                .setSalary(salary)
                .build();
    }

    // ─── Unary Call: GetEmployee ─────────────────────────────────────────
    @Override
    public void getEmployee(GetEmployeeRequest request,
                            StreamObserver<GetEmployeeResponse> responseObserver) {

        String requestedId = request.getEmployeeId();
        System.out.println("[GetEmployee] Request received for ID: " + requestedId);

        Employee found = EMPLOYEES.stream()
                .filter(e -> e.getId().equals(requestedId))
                .findFirst()
                .orElse(null);

        if (found == null) {
            // Send error back to caller if ID not found
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription("Employee not found: " + requestedId)
                    .asRuntimeException()
            );
            return;
        }

        GetEmployeeResponse response = GetEmployeeResponse.newBuilder()
                .setEmployee(found)
                .build();

        responseObserver.onNext(response);    // send the response
        responseObserver.onCompleted();       // signal we're done
        System.out.println("[GetEmployee] Returned: " + found.getName());
    }

    // ─── Server Streaming Call: ListEmployees ────────────────────────────
    @Override
    public void listEmployees(ListEmployeesRequest request,
                              StreamObserver<Employee> responseObserver) {

        String department = request.getDepartment();
        System.out.println("[ListEmployees] Request received for department: " + department);

        long count = EMPLOYEES.stream()
                .filter(e -> e.getDepartment().equalsIgnoreCase(department))
                .peek(e -> {
                    responseObserver.onNext(e);   // stream each employee one by one
                    System.out.println("[ListEmployees] Streaming: " + e.getName());
                })
                .count();

        if (count == 0) {
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription("No employees found in department: " + department)
                    .asRuntimeException()
            );
            return;
        }

        responseObserver.onCompleted();    // signal end of stream
        System.out.println("[ListEmployees] Done. Total sent: " + count);
    }
}