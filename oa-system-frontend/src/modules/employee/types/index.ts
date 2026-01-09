// 员工状态枚举
export enum EmployeeStatus {
  Active = 'active',
  Inactive = 'inactive',
  Probation = 'probation',
  Leave = 'leave',
}

// 试用期状态枚举
export enum ProbationStatus {
  Probation = 'probation',
  Regular = 'regular',
}

// 性别枚举
export enum Gender {
  Male = 'male',
  Female = 'female',
}

// 员工信息接口
export interface Employee {
  id: string
  employeeNo: string
  name: string
  englishName?: string
  gender: Gender
  avatar?: string
  department: string
  departmentId: string
  position: string
  positionId?: string
  phone: string
  email: string
  status: EmployeeStatus
  probationStatus: ProbationStatus
  entryDate: string
  regularDate?: string
  birthDate?: string
  officeLocation?: string
  emergencyContact?: string
  emergencyPhone?: string
  superior?: string
  superiorId?: string
  workYears?: number
  lastLoginTime?: string
  createTime: string
  updateTime: string
}

// 员工筛选条件接口
export interface EmployeeFilter {
  keyword?: string
  status?: EmployeeStatus | ''
  departmentIds?: string[]
  probationStatus?: ProbationStatus | ''
  position?: string
  gender?: Gender | ''
  entryDateRange?: [string, string]
}

// 员工表单接口
export interface EmployeeForm {
  id?: string
  employeeNo: string
  name: string
  englishName?: string
  gender: Gender
  phone: string
  email: string
  department: string
  departmentId: string
  position: string
  positionId?: string
  entryDate: string
  regularDate?: string
  birthDate?: string
  officeLocation?: string
  avatar?: string
  emergencyContact?: string
  emergencyPhone?: string
  superior?: string
  superiorId?: string
}

// 操作记录接口
export interface OperationLog {
  id: string
  employeeId: string
  operation: string
  operator: string
  operatorId: string
  createTime: string
  details?: Record<string, any>
}
