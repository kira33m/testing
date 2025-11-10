package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Spy
    EmployeeMapper employeeMapper;

    @Mock
    EmployeeRepository employeeRepository;

    @InjectMocks
    EmployeeService employeeService;

    @Captor
    ArgumentCaptor<Employee> employeeCaptor;

    private Employee employee;
    private EmployeeResponse employeeResponse;
    private EmployeeRequest employeeRequest;
    private Department department;
    private Employee manager;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("IT");

        manager = new Employee();
        manager.setId(2L);
        manager.setName("Jane Manager");

        employee = new Employee();
        employee.setId(1L);
        employee.setName("Jane Doe");
        employee.setPosition("Developer");
        employee.setSalary(100000L);
        employee.setDepartment(department);
        employee.setManager(manager);

        employeeResponse = new EmployeeResponse(
            1L,
            "Jane Doe",
            "Developer",
            100000L,
            "IT",
            "Jane Manager"
        );

        employeeRequest = new EmployeeRequest();
        employeeRequest.setName("Jane Doe");
        employeeRequest.setPosition("Developer");
        employeeRequest.setSalary(100000L);
        employeeRequest.setDepartmentId(1L);
        employeeRequest.setManagerId(2L);
    }

    @Test
    @DisplayName("getAllEmployees - should return list when employees exist")
    void getAllEmployees_ShouldReturnList_WhenEmployeesExist() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        var result = employeeService.getAllEmployees();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(employeeResponse);

        verify(employeeRepository, times(1)).findAll();
        verify(employeeMapper, times(1)).toResponse(employee);
    }

    @Test
    @DisplayName("getAllEmployees - should return empty list when no employees")
    void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployees() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        var result = employeeService.getAllEmployees();

        assertThat(result).isEmpty();

        verify(employeeRepository, times(1)).findAll();
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("getEmployeeById - should return response when employee found")
    void getEmployeeById_ShouldReturnResponse_WhenEmployeeFound() {
        var employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        var result = employeeService.getEmployeeById(employeeId);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(employeeResponse);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeMapper, times(1)).toResponse(employee);
        verify(employeeMapper, times(1)).toResponse(employeeCaptor.capture());
    }

    @Test
    @DisplayName("getEmployeeById - should throw exception when employee not found")
    void getEmployeeById_ShouldThrowException_WhenEmployeeNotFound() {
        var employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(employeeId));

        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("createEmployee - should create and return response when request valid")
    void createEmployee_ShouldCreateAndReturnResponse_WhenRequestValid() {
        when(employeeMapper.toEntity(employeeRequest)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        var result = employeeService.createEmployee(employeeRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(employeeResponse);

        verify(employeeMapper, times(1)).toEntity(employeeRequest);
        verify(employeeRepository, times(1)).save(employee);
        verify(employeeMapper, times(1)).toResponse(employee);
    }

    @Test
    @DisplayName("createEmployee - should handle null manager when managerId null")
    void createEmployee_ShouldHandleNullManager_WhenManagerIdNull() {
        employeeRequest.setManagerId(null);
        employee.setManager(null);
        var updatedResponse = new EmployeeResponse(1L, "Jane Doe", "Developer", 100000L, "IT", "Нет менеджера");

        when(employeeMapper.toEntity(employeeRequest)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(updatedResponse);

        var result = employeeService.createEmployee(employeeRequest);

        assertThat(result.managerName()).isEqualTo("Нет менеджера");

        verify(employeeMapper, times(1)).toEntity(employeeRequest);
        verify(employeeRepository, times(1)).save(employee);
        verify(employeeMapper, times(1)).toResponse(employee);
    }

    @Test
    @DisplayName("updateEmployee - should update core fields and return response when employee exists")
    void updateEmployee_ShouldUpdateCoreFieldsAndReturnResponse_WhenEmployeeExists() {
        var employeeId = 1L;
        var existingEmployee = new Employee();
        existingEmployee.setId(employeeId);
        existingEmployee.setName("Old Name");
        existingEmployee.setPosition("Old Position");
        existingEmployee.setSalary(50000L);
        existingEmployee.setDepartment(department);
        existingEmployee.setManager(manager);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(existingEmployee)).thenReturn(existingEmployee);
        when(employeeMapper.toResponse(existingEmployee)).thenReturn(employeeResponse);

        var result = employeeService.updateEmployee(employeeId, employeeRequest);

        assertThat(result).isEqualTo(employeeResponse);
        assertThat(existingEmployee.getName()).isEqualTo("Jane Doe");
        assertThat(existingEmployee.getPosition()).isEqualTo("Developer");
        assertThat(existingEmployee.getSalary()).isEqualTo(100000L);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).save(existingEmployee);
        verify(employeeMapper, times(1)).toResponse(existingEmployee);
    }

    @Test
    @DisplayName("updateEmployee - should not update department and manager when employee exists")
    void updateEmployee_ShouldNotUpdateDepartmentAndManager_WhenEmployeeExists() {
        var employeeId = 1L;
        var existingEmployee = new Employee();
        existingEmployee.setId(employeeId);
        existingEmployee.setDepartment(department);
        existingEmployee.setManager(manager);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(existingEmployee)).thenReturn(existingEmployee);
        when(employeeMapper.toResponse(existingEmployee)).thenReturn(employeeResponse);

        employeeRequest.setDepartmentId(99L);
        employeeRequest.setManagerId(99L);

        employeeService.updateEmployee(employeeId, employeeRequest);

        assertThat(existingEmployee.getDepartment()).isEqualTo(department);
        assertThat(existingEmployee.getManager()).isEqualTo(manager);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).save(existingEmployee);
        verify(employeeMapper, times(1)).toResponse(existingEmployee);
    }

    @Test
    @DisplayName("updateEmployee - should throw exception when employee not found")
    void updateEmployee_ShouldThrowException_WhenEmployeeNotFound() {
        var employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(employeeId, employeeRequest));

        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("updateEmployee - should update with new salary when salary changed")
    void updateEmployee_ShouldUpdateWithNewSalary_WhenSalaryChanged() {
        var employeeId = 1L;
        var existingEmployee = new Employee();
        existingEmployee.setId(employeeId);
        existingEmployee.setSalary(50000L);

        employeeRequest.setSalary(100000L);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(existingEmployee)).thenReturn(existingEmployee);
        when(employeeMapper.toResponse(existingEmployee)).thenReturn(employeeResponse);

        employeeService.updateEmployee(employeeId, employeeRequest);

        assertThat(existingEmployee.getSalary()).isEqualTo(100000L);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).save(existingEmployee);
        verify(employeeMapper, times(1)).toResponse(existingEmployee);
    }

    @Test
    @DisplayName("deleteEmployee - should delete when employee exists")
    void deleteEmployee_ShouldDelete_WhenEmployeeExists() {
        var employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(employeeId);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).deleteById(employeeId);
    }

    @Test
    @DisplayName("deleteEmployee - should throw exception when employee not found")
    void deleteEmployee_ShouldThrowException_WhenEmployeeNotFound() {
        var employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(employeeId));

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(0)).deleteById(employeeId);
    }

    @Test
    @DisplayName("deleteEmployee - should not delete without check when not found")
    void deleteEmployee_ShouldNotDeleteWithoutCheck_WhenNotFound() {
        var employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(employeeId));

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(0)).deleteById(employeeId);
    }
}