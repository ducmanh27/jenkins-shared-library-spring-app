def call() {
    def branch = env.BRANCH_NAME ?: ''
    def tag = env.GIT_TAG ?: ''
    return branch ==~ /^release\/.*/ || tag ==~ /^v\d+\.\d+\.\d+$/
}
