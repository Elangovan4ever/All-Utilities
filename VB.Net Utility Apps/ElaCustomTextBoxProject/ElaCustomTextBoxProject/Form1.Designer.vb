<Global.Microsoft.VisualBasic.CompilerServices.DesignerGenerated()>
Partial Class Form1
    Inherits System.Windows.Forms.Form

    'Form overrides dispose to clean up the component list.
    <System.Diagnostics.DebuggerNonUserCode()>
    Protected Overrides Sub Dispose(ByVal disposing As Boolean)
        Try
            If disposing AndAlso components IsNot Nothing Then
                components.Dispose()
            End If
        Finally
            MyBase.Dispose(disposing)
        End Try
    End Sub

    'Required by the Windows Form Designer
    Private components As System.ComponentModel.IContainer

    'NOTE: The following procedure is required by the Windows Form Designer
    'It can be modified using the Windows Form Designer.  
    'Do not modify it using the code editor.
    <System.Diagnostics.DebuggerStepThrough()>
    Private Sub InitializeComponent()
        Me.Button1 = New System.Windows.Forms.Button()
        Me.ElaCustomTextBox2 = New ElaCustomTextBoxProject.ElaCustomTextBox()
        Me.ElaCustomTextBox1 = New ElaCustomTextBoxProject.ElaCustomTextBox()
        Me.SuspendLayout()
        '
        'Button1
        '
        Me.Button1.Location = New System.Drawing.Point(290, 26)
        Me.Button1.Name = "Button1"
        Me.Button1.Size = New System.Drawing.Size(75, 23)
        Me.Button1.TabIndex = 0
        Me.Button1.Text = "Button1"
        Me.Button1.UseVisualStyleBackColor = True
        '
        'ElaCustomTextBox2
        '
        Me.ElaCustomTextBox2.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomTextBox2.BorderColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomTextBox2.BorderColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomTextBox2.BorderThickness = ElaCustomTextBoxProject.ElaCustomTextBox.BorderThicknessEnum.Thick
        Me.ElaCustomTextBox2.Location = New System.Drawing.Point(290, 113)
        Me.ElaCustomTextBox2.Name = "ElaCustomTextBox2"
        Me.ElaCustomTextBox2.Size = New System.Drawing.Size(100, 20)
        Me.ElaCustomTextBox2.TabIndex = 2
        '
        'ElaCustomTextBox1
        '
        Me.ElaCustomTextBox1.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomTextBox1.BorderColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomTextBox1.BorderColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomTextBox1.BorderThickness = ElaCustomTextBoxProject.ElaCustomTextBox.BorderThicknessEnum.Normal
        Me.ElaCustomTextBox1.Location = New System.Drawing.Point(290, 72)
        Me.ElaCustomTextBox1.Name = "ElaCustomTextBox1"
        Me.ElaCustomTextBox1.Size = New System.Drawing.Size(100, 20)
        Me.ElaCustomTextBox1.TabIndex = 1
        '
        'Form1
        '
        Me.AutoScaleDimensions = New System.Drawing.SizeF(6.0!, 13.0!)
        Me.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font
        Me.BackColor = System.Drawing.Color.White
        Me.ClientSize = New System.Drawing.Size(1195, 568)
        Me.Controls.Add(Me.ElaCustomTextBox2)
        Me.Controls.Add(Me.ElaCustomTextBox1)
        Me.Controls.Add(Me.Button1)
        Me.Name = "Form1"
        Me.Text = "Form1"
        Me.WindowState = System.Windows.Forms.FormWindowState.Maximized
        Me.ResumeLayout(False)
        Me.PerformLayout()

    End Sub
    Friend WithEvents Button1 As Button
    Friend WithEvents ElaCustomTextBox1 As ElaCustomTextBox
    Friend WithEvents ElaCustomTextBox2 As ElaCustomTextBox
End Class
